#define _CRT_SECURE_NO_WARNINGS
#include <iostream>
#include <chrono>
#include <omp.h>


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

void gaussian_blur_separate_parallel(const char* filename)
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



#pragma omp parallel for
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

# pragma omp parallel for 
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
	printf("Gaussian Blur Separate - Parallel: Time %dms\n", time);

	// Write the blurred image into a JPG file
	stbi_write_jpg("blurred_image_parallel.jpg", width, height, 4/*channels*/, img_out, 90 /*quality*/);

	stbi_image_free(img_in);
	delete[] img_horizontal_blur;
	delete[] img_out;
}

void bloom_parallel(const char* filename) {
	int width = 0;
	int height = 0;
	int img_orig_channels = 4;

	// 1. load the image
	unsigned char* img_in = stbi_load(filename, &width, &height, &img_orig_channels, 4);
	if (img_in == nullptr) {
		printf("Could not load %s\n", filename);
		return;
	}

	int total_pixels = width * height;
	

	auto start = std::chrono::high_resolution_clock::now();

	// 2. calculate the max luminance with no wait and critical 
	float max_luminance = 0.0f;

#pragma omp parallel
	{
		float local_max = 0.0f;

#pragma omp for nowait
		for (int i = 0; i < total_pixels; i++) {
			float r = img_in[4 * i + 0];
			float g = img_in[4 * i + 1];
			float b = img_in[4 * i + 2];
			float lum = (r + g + b) / 3.0f;
			if (lum > local_max) local_max = lum;
		}
		// the critical variable is in a mutex
#pragma omp critical
		{
			if (local_max > max_luminance) max_luminance = local_max;
		}
	}

#pragma omp single
	printf("Max luminance = %.2f\n", max_luminance);

	// 3. create the bloom_mask
	unsigned char* bloom_mask = new unsigned char[total_pixels * 4];
	float threshold = 0.9f * max_luminance;


#pragma omp parallel for
	for (int i = 0; i < total_pixels; i++) {
		float r = img_in[4 * i + 0];
		float g = img_in[4 * i + 1];
		float b = img_in[4 * i + 2];
		float luminance = (r + g + b) / 3.0f;

		if (luminance > threshold) {
			bloom_mask[4 * i + 0] = img_in[4 * i + 0];
			bloom_mask[4 * i + 1] = img_in[4 * i + 1];
			bloom_mask[4 * i + 2] = img_in[4 * i + 2];
			bloom_mask[4 * i + 3] = img_in[4 * i + 3];
		}
		else {
			bloom_mask[4 * i + 0] = 0;
			bloom_mask[4 * i + 1] = 0;
			bloom_mask[4 * i + 2] = 0;
			bloom_mask[4 * i + 3] = img_in[4 * i + 3]; 
		}
	}

	// 4.Gaussian blur
	unsigned char* bloom_horizontal = new unsigned char[total_pixels * 4];
	unsigned char* bloom_blurred = new unsigned char[total_pixels * 4];

	// Horizontal blur
#pragma omp parallel for 
	for (int y = 0; y < height; y++) {
		for (int x = 0; x < width; x++) {
			int pixel = y * width + x;
			for (int channel = 0; channel < 4; channel++) {
				bloom_horizontal[4 * pixel + channel] = blurAxis(x, y, channel, 0, bloom_mask, width, height);
			}
		}
	}

	// Vertical blur
#pragma omp parallel for 
	for (int y = 0; y < height; y++) {
		for (int x = 0; x < width; x++) {
			int pixel = y * width + x;
			for (int channel = 0; channel < 4; channel++) {
				bloom_blurred[4 * pixel + channel] = blurAxis(x, y, channel, 1, bloom_horizontal, width, height);
			}
		}
	}

	// 5 & 6. save the  blurred  image and create and save the final image
	unsigned char* final_img = new unsigned char[total_pixels * 4];

#pragma omp parallel
	{
#pragma omp sections
		{ // use sections to parallel this code 
#pragma omp section
			{
				// save the blured image
				stbi_write_jpg("bloom_blurred.jpg", width, height, 4, bloom_blurred, 90);
			}

#pragma omp section
			{
				// create and save the final image
				// also a block of code that can be paralleled
#pragma omp parallel for
				for (int i = 0; i < total_pixels; i++) {
					for (int channel = 0; channel < 3; channel++) {
						int sum = img_in[4 * i + channel] + bloom_blurred[4 * i + channel];
						final_img[4 * i + channel] = (unsigned char)std::min(sum, 255);
					}
					final_img[4 * i + 3] = img_in[4 * i + 3]; // alpha
				}

				// 7. save the final image
				stbi_write_jpg("bloom_final.jpg", width, height, 4, final_img, 90);
			}
		}
	}

	auto end = std::chrono::high_resolution_clock::now();
	int duration_ms = std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
	printf("Bloom filter completed in %d ms\n", duration_ms);


	// clear the memory
	stbi_image_free(img_in);
	delete[] bloom_mask;
	delete[] bloom_horizontal;
	delete[] bloom_blurred;
	delete[] final_img;
}



int main()
{
	//const char* filename = "garden.jpg";
	//gaussian_blur_serial(filename);

	// gaussian_blur_parallel(filename);

	const char* filename2 = "street_night.jpg";
	gaussian_blur_separate_serial(filename2);
	gaussian_blur_separate_parallel(filename2);

	bloom_parallel(filename2);

	// gaussian_blur_separate_parallel(filename2);

	return 0;
}