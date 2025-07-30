#  Parallel Image Processing with OpenMP

This project implements **Gaussian Blur** and **Bloom Effect** filters using **OpenMP** in C++. The goal is to explore the performance benefits of parallel execution compared to serial processing in image manipulation tasks.

---

##  Overview

The project includes:

- `gaussian_blur_separate_parallel()`: Applies Gaussian blur using OpenMP.
- `bloom_parallel()`: Applies a bloom effect in multiple stages using OpenMP directives.
- Performance measurements comparing serial and parallel execution.

---

##  Technologies

- **C++17**
- **OpenMP** (`#pragma omp`)
- Image I/O via `stb_image` and `stb_image_write` (or equivalent)
- Multicore execution with `g++ -fopenmp`

---

##  How It Works

###  Part A – Gaussian Blur with OpenMP

Function: `gaussian_blur_separate_parallel()`

- Uses `#pragma omp parallel for` to parallelize:
  - Horizontal blur loop
  - Vertical blur loop
- Results:
  - Mean Serial Time: **2255.25 ms**
  - Mean Parallel Time: **418.75 ms**
  - **Speedup ≈ 5.39x**

**Conclusion**: OpenMP greatly accelerates the Gaussian blur process by distributing pixel rows across threads.

---

###  Part B – Bloom Effect with OpenMP

Function: `bloom_parallel()`

Applies a bloom filter in 7 parallel stages:

1. **Max Luminance Detection**:
   - Parallel loop with `#pragma omp for nowait`
   - Shared update with `#pragma omp critical`

2. **Print Max Luminance**:
   - One thread prints the result using `#pragma omp single`

3. **Bloom Mask Creation**:
   - Simple loop over pixels with `#pragma omp parallel for`

4. **Gaussian Blur** (2 passes):
   - Horizontal and vertical blur loops with `#pragma omp parallel for`

5. **Save Blurred Image**

6. **Merge Bloom Mask with Original Image**

7. **Save Final Image**

Steps 5–7 run in parallel using `#pragma omp sections`, allowing image saving and merging to happen simultaneously.

---

##  Performance Summary

| Execution Type     | Mean Time (ms) |
|--------------------|----------------|
| Gaussian Blur Serial  | 2255.25        |
| Gaussian Blur Parallel (OpenMP) | 418.75         |
| Speedup           | ~5.39×         |

---

