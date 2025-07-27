#define _CRT_SECURE_NO_WARNINGS
#include <iostream>
#include <chrono>
#include <thread>
#include <atomic>
#include <vector>
#include <mutex>
#include <barrier>
#include <cstring> 


#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"

#define STB_IMAGE_WRITE_IMPLEMENTATION
#include "stb_image_write.h"

const int KERNEL_RADIUS = 8;
const float sigma = 3.f;

unsigned char blur(int x, int y, int channel, unsigned char* input, int width, int height)
{
	float sum_weight = 0.0f;
	float ret = 0.f;

	for (int offset_y = -KERNEL_RADIUS; offset_y <= KERNEL_RADIUS; offset_y++)
	{
		for (int offset_x = -KERNEL_RADIUS; offset_x <= KERNEL_RADIUS; offset_x++)
		{
			int pixel_y = std::max(std::min(y + offset_y, height - 1), 0);
			int pixel_x = std::max(std::min(x + offset_x, width - 1), 0);
			int pixel = pixel_y * width + pixel_x;

			float weight = std::exp(-(offset_x * offset_x + offset_y * offset_y) / (2.f * sigma * sigma));

			ret += weight * input[4 * pixel + channel];
			sum_weight += weight;
		}
	}
	ret /= sum_weight;

	return (unsigned char)std::max(std::min(ret, 255.f), 0.f);
}

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

void gaussian_blur_serial(const char* filename)
{
	int width = 0;
	int height = 0;
	int img_orig_channels = 4;
	// Load an image into an array of unsigned chars that is the size of [width * height * number of channels]. The channels are the Red, Green, Blue and Alpha channels of the image.
	unsigned char* img_in = stbi_load(filename, &width, &height, &img_orig_channels /*image file channels*/, 4 /*requested channels*/);

	std::cout << "Height: "<< height << " \n Width: " << width << std::endl;
	if (img_in == nullptr)
	{
		printf("Could not load %s\n", filename);
		return;
	}

	unsigned char* img_out = new unsigned char[width * height * 4];

	// Timer to measure performance
	auto start = std::chrono::high_resolution_clock::now();

	// Perform Gaussian Blur to each pixel
	for (int y = 0; y < height; y++)
	{
		for (int x = 0; x < width; x++)
		{
			int pixel = y * width + x;
			for (int channel = 0; channel < 4; channel++)
			{
				img_out[4 * pixel + channel] = blur(x, y, channel, img_in, width, height);
			}
		}
	}

	// Timer to measure performance
	auto end = std::chrono::high_resolution_clock::now();
	// Computation time in milliseconds
	int time = (int)std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
	printf("Gaussian Blur - Serial: Time %dms\n", time);

	// Write the blurred image into a JPG file
	stbi_write_jpg("blurred_image_serial.jpg", width, height, 4, img_out, 90 /*quality*/);

	stbi_image_free(img_in);
	delete[] img_out;
}

// applies blur to an image
void blur_worker(int start_y, int end_y, int width, int height, unsigned char* img_in, unsigned char* img_out) {
	for (int y = start_y; y < end_y; ++y) {
		for (int x = 0; x < width; ++x) {
			int pixel = y * width + x;
			for (int c = 0; c < 4; ++c) {
				img_out[4 * pixel + c] = blur(x, y, c, img_in, width, height);
			}
		}
	}
}

void gaussian_blur_parallel(const char* filename) {
	int width = 0;
	int height = 0;
	int img_orig_channels = 4;
	// Load an image into an array of unsigned chars that is the size of [width * height * number of channels]. The channels are the Red, Green, Blue and Alpha channels of the image.
	unsigned char* img_in = stbi_load(filename, &width, &height, &img_orig_channels /*image file channels*/, 4 /*requested channels*/);
	if (!img_in) {
		std::cerr << "Could not load " << filename << std::endl;
		return;
	}

	unsigned char* img_out = new unsigned char[width * height * 4];

	auto start = std::chrono::high_resolution_clock::now();

	// Define number of threads and rows per thread
	int threads_num = 8;
	int rows_per_thread = height / threads_num;


	// create 8 threads, each processing a part of the image
	std::thread t1(blur_worker, 0 * rows_per_thread, 1 * rows_per_thread, width, height, img_in, img_out);
	std::thread t2(blur_worker, 1 * rows_per_thread, 2 * rows_per_thread, width, height, img_in, img_out);
	std::thread t3(blur_worker, 2 * rows_per_thread, 3 * rows_per_thread, width, height, img_in, img_out);
	std::thread t4(blur_worker, 3 * rows_per_thread, 4 * rows_per_thread, width, height, img_in, img_out);
	std::thread t5(blur_worker, 4 * rows_per_thread, 5 * rows_per_thread, width, height, img_in, img_out);
	std::thread t6(blur_worker, 5 * rows_per_thread, 6 * rows_per_thread, width, height, img_in, img_out);
	std::thread t7(blur_worker, 6 * rows_per_thread, 7 * rows_per_thread, width, height, img_in, img_out);
	std::thread t8(blur_worker, 7 * rows_per_thread, height, width, height, img_in, img_out);

	// Wait for all threads to finish
	t1.join();
	t2.join();
	t3.join();
	t4.join();
	t5.join();
	t6.join();
	t7.join();
	t8.join();

	auto end = std::chrono::high_resolution_clock::now();
	int time = std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
	std::cout << "Gaussian Blur - Parallel (8 threads): Time " << time << "ms\n";

	stbi_write_jpg("blurred_image_parallel.jpg", width, height, 4, img_out, 90);

	stbi_image_free(img_in);
	delete[] img_out;
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

void gaussian_blur_separate_parallel(const char* filename) {
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

	const int num_pixels = width * height;
	unsigned char* img_normalized = new unsigned char[width * height * 4];


	std::atomic<unsigned char> max_vals[4] = { 0, 0, 0, 0 };

	// Barrier to wait all threads
	std::barrier barrier(4);  

	// Lambda function for thread work
	auto thread_work = [&](int thread_id) {
		int pixels_per_thread = num_pixels / 4;
		int start = thread_id * pixels_per_thread;
		int end = (thread_id == 3) ? num_pixels : start + pixels_per_thread;

		// Step one, compute max values 
		unsigned char local_max[4] = { 0, 0, 0, 0 };
		for (int i = start; i < end; ++i) {
			for (int c = 0; c < 4; ++c) {
				local_max[c] = std::max(local_max[c], img_in[4 * i + c]);
			}
		}
		// Atomically update global max values
		for (int c = 0; c < 4; ++c) {
			unsigned char current = max_vals[c].load();
			while (current < local_max[c] && !max_vals[c].compare_exchange_weak(current, local_max[c]));
		}

		barrier.arrive_and_wait(); // Wait for all threads to finish max computation

		// Step two, normalize pixels
		for (int i = start; i < end; ++i) {
			for (int c = 0; c < 4; ++c) {
				if (max_vals[c] != 0)
					img_normalized[4 * i + c] = static_cast<unsigned char>(255.f * img_in[4 * i + c] / max_vals[c]);
				else
					img_normalized[4 * i + c] = img_in[4 * i + c]; 
			}
		}

		barrier.arrive_and_wait();  // Wait for all threads to complete normalization

		// One thread saves the normalized image
		if (thread_id == 0) {
			stbi_write_jpg("image_normalized.jpg", width, height, 4, img_normalized, 90);
		}

		barrier.arrive_and_wait(); // Ensure image is saved before continuing

		// Step three, horizontal blur
		for (int i = start; i < end; ++i) {
			int y = i / width;
			int x = i % width;
			for (int c = 0; c < 4; ++c) {
				img_horizontal_blur[4 * i + c] = blurAxis(x, y, c, 0, img_normalized, width, height);
			}
		}

		barrier.arrive_and_wait();  // Wait for all threads to finish horizontal blur

		// One thread saves the horizontal image
		if (thread_id == 1) {
			stbi_write_jpg("image_blurred_horizontal.jpg", width, height, 4, img_horizontal_blur, 90);
		}

		barrier.arrive_and_wait(); // ensure image is saved

		// Step four, vertical blur
		for (int i = start; i < end; ++i) {
			int y = i / width;
			int x = i % width;
			for (int c = 0; c < 4; ++c) {
				img_out[4 * i + c] = blurAxis(x, y, c, 1, img_horizontal_blur, width, height);
			}
		}

		barrier.arrive_and_wait(); // Wait for all threads to finish vertical blur

		// One thread saves the final image
		if (thread_id == 2) {
			stbi_write_jpg("image_blurred_final.jpg", width, height, 4, img_out, 90);
		}

		barrier.arrive_and_wait(); // ensure the final image is saved
	};

	// Timer to measure performance
	auto start = std::chrono::high_resolution_clock::now();

	std::thread t0(thread_work, 0);
	std::thread t1(thread_work, 1);
	std::thread t2(thread_work, 2);
	std::thread t3(thread_work, 3);

	t0.join();
	t1.join();
	t2.join();
	t3.join();

	// Timer to measure performance
	auto end = std::chrono::high_resolution_clock::now();
	// Computation time in milliseconds
	int time = (int)std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
	printf("Gaussian Blur Separate - Parallel with 4 threads: Time %dms\n", time);

	stbi_image_free(img_in);
	delete[] img_normalized;
	delete[] img_horizontal_blur;
	delete[] img_out;
}

int main()
{
	const char* filename = "garden.jpg";
	gaussian_blur_serial(filename);

	gaussian_blur_parallel(filename);

	
	
	const char* filename2 = "street_night.jpg";
	gaussian_blur_separate_serial(filename2);

	gaussian_blur_separate_parallel(filename2);

	return 0;
}