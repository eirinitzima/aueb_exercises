#define _CRT_SECURE_NO_WARNINGS
#define CL_TARGET_OPENCL_VERSION 200
#define NOMINMAX

#include <thread>
#include <atomic>
#include <algorithm>

#include <vector>
#include <mutex>
#include <barrier>
#include <cstring> 

#include <CL/cl.hpp>
#include <chrono>
#include <iostream>
#include <fstream>
#include <vector>
#include <cmath>

#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"

#define STB_IMAGE_WRITE_IMPLEMENTATION
#include "stb_image_write.h"

const int KERNEL_RADIUS = 8;
const float sigma = 3.f;


unsigned char blurAxis(int x, int y, int channel, int axis/*0: horizontal axis, 1: vertical axis*/, unsigned char* input, int width, int height)
{
    float sum_weight = 0.0f;
    float ret = 0.f;

    for (int offset = -KERNEL_RADIUS; offset <= KERNEL_RADIUS; offset++)
    {
        int offset_x = axis == 0 ? offset : 0;
        int offset_y = axis == 1 ? offset : 0;
        int pixel_y = std::max(std::min(y + offset_y, height - 1), 0);
        int pixel_x = std::max(std::min(x + offset_x, width - 1), 0);
        int pixel = pixel_y * width + pixel_x;

        float weight = std::exp(-(offset * offset) / (2.f * sigma * sigma));

        ret += weight * input[4 * pixel + channel];
        sum_weight += weight;
    }
    ret /= sum_weight;
    return (unsigned char)std::max(std::min(ret, 255.f), 0.f);
}

void gaussian_blur_separate_serial(const char* filename)
{
    int width = 0;
    int height = 0;
    int img_orig_channels = 4;
    // Load an image into an array of unsigned chars that is the size of width * height * number of channels. The channels are the Red, Green, Blue and Alpha channels of the image.
    unsigned char* img_in = stbi_load(filename, &width, &height, &img_orig_channels /*image file channels*/, 4 /*requested channels*/);
    if (img_in == nullptr)
    {
        printf("Could not load %s\n", filename);
        return;
    }

    unsigned char* img_horizontal_blur = new unsigned char[width * height * 4];
    unsigned char* img_out = new unsigned char[width * height * 4];

    // Timer to measure performance
    auto start = std::chrono::high_resolution_clock::now();

    // Horizontal Blur
    for (int y = 0; y < height; y++)
    {
        for (int x = 0; x < width; x++)
        {
            int pixel = y * width + x;
            for (int channel = 0; channel < 4; channel++)
            {
                img_horizontal_blur[4 * pixel + channel] = blurAxis(x, y, channel, 0, img_in, width, height);
            }
        }
    }
    // Vertical Blur
    for (int y = 0; y < height; y++)
    {
        for (int x = 0; x < width; x++)
        {
            int pixel = y * width + x;
            for (int channel = 0; channel < 4; channel++)
            {
                img_out[4 * pixel + channel] = blurAxis(x, y, channel, 1, img_horizontal_blur, width, height);
            }
        }
    }
    // Timer to measure performance
    auto end = std::chrono::high_resolution_clock::now();
    // Computation time in milliseconds
    int time = (int)std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
    printf("Gaussian Blur Separate - Serial: Time %dms\n", time);

    // Write the blurred image into a JPG file
    stbi_write_jpg("blurred_separate.jpg", width, height, 4/*channels*/, img_out, 90 /*quality*/);

    stbi_image_free(img_in);
    delete[] img_horizontal_blur;
    delete[] img_out;
}

std::vector<float> computeGaussianWeights(int radius, float sigma) {
    std::vector<float> weights(2 * radius + 1);
    float sum = 0.f;
    for (int i = -radius; i <= radius; ++i) {
        float weight = std::exp(-(i * i) / (2.f * sigma * sigma));
        weights[i + radius] = weight;
        sum += weight;
    }
    for (float& w : weights) w /= sum;
    return weights;
}

void gaussian_blur_separate_parallel(const char* filename) {
    int width = 0;
    int height = 0;
    int img_orig_channels = 4;
    // Load an image into an array of unsigned chars that is the size of [width * height * number of channels]. The channels are the Red, Green, Blue and Alpha channels of the image.
    unsigned char* img_in = stbi_load(filename, &width, &height, &img_orig_channels /*image file channels*/, 4 /*requested channels*/);

    if (img_in == nullptr)
    {
        printf("Could not load %s\n", filename);
        return;
    }

    size_t img_size = width * height * 4;
    std::vector<float> weights = computeGaussianWeights(KERNEL_RADIUS, sigma);
    size_t weights_size = weights.size() * sizeof(float);

    // Timer to measure performance
    auto start = std::chrono::high_resolution_clock::now();

    try {
        // Platform, Device & Context
        std::vector<cl::Platform> platforms;
        cl::Platform::get(&platforms);
        if (platforms.empty()) throw std::runtime_error("No OpenCL platforms found.");

        cl::Platform platform = platforms.front();
        std::vector<cl::Device> devices;
        platform.getDevices(CL_DEVICE_TYPE_GPU, &devices);
        if (devices.empty()) platform.getDevices(CL_DEVICE_TYPE_CPU, &devices);
        if (devices.empty()) throw std::runtime_error("No OpenCL devices found.");

        cl::Device device = devices.front();
        cl::Context context(device);
        cl::CommandQueue queue(context, device);

        // Load kernel
        std::ifstream sourceFile("kernel.cl");
        if (!sourceFile.is_open()) throw std::runtime_error("Cannot open kernel.cl");
        std::string sourceCode(std::istreambuf_iterator<char>(sourceFile), {});
        cl::Program program(context, sourceCode);
        program.build({ device });

        cl::Kernel kernel(program, "gaussian_blur_axis");

        // Buffers
        cl::Buffer buf_in(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, img_size, img_in);
        cl::Buffer buf_intermediate(context, CL_MEM_READ_WRITE, img_size);
        cl::Buffer buf_out(context, CL_MEM_WRITE_ONLY, img_size);
        cl::Buffer buf_weights(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, weights_size, weights.data());

        // First pass - horizontal
        kernel.setArg(0, buf_in);
        kernel.setArg(1, buf_intermediate);
        kernel.setArg(2, buf_weights);
        kernel.setArg(3, width);
        kernel.setArg(4, height);
        kernel.setArg(5, 0); // axis
        kernel.setArg(6, KERNEL_RADIUS);
        cl::NDRange global(width, height);
        cl::NDRange local(32, 32); 

        queue.enqueueNDRangeKernel(kernel, cl::NullRange, global, local);


        // Second pass - vertical
        kernel.setArg(0, buf_intermediate);
        kernel.setArg(1, buf_out);
        kernel.setArg(5, 1); // axis vertical
        

        queue.enqueueNDRangeKernel(kernel, cl::NullRange, global, local);


        // reuslts
        std::vector<unsigned char> img_out(img_size);
        queue.enqueueReadBuffer(buf_out, CL_TRUE, 0, img_size, img_out.data());

        stbi_write_jpg("image_blurred_final.jpg", width, height, 4, img_out.data(), 90);
    }
 
    catch (std::exception& e) {
        std::cerr << "Error: " << e.what() << "\n";
    }

    stbi_image_free(img_in);


    // Timer to measure performance
    auto end = std::chrono::high_resolution_clock::now();
    // Computation time in milliseconds
    int time = (int)std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
    printf("Gaussian Blur Separate - Parallel: Time %dms\n", time);


}

int main() {
    const char* filename2 = "street_night.jpg";
    
    gaussian_blur_separate_serial(filename2);

    gaussian_blur_separate_parallel(filename2);
    return 0;
}
