#  Gaussian Blur with Parallel Threads in C++

This project demonstrates the application of the **Gaussian Blur** algorithm on images using **parallel programming** techniques in **C++**. The implementation evaluates performance across different thread counts (2, 4, and 8) and compares it to a serial approach.

---

## Overview

- Implements Gaussian Blur in C++
- Uses multithreading for performance improvements
- Compares serial and parallel execution times
- Written as part of a university assignment
- Supports a second version using `std::barrier` for organized multistage processing

---

##  Features

### Question (a)
- Implements `gaussian_blur_parallel(...)` which:
  - Splits image rows across threads
  - Each thread runs a `blur_worker(...)` to process its portion
- No shared memory access during blur → no synchronization issues
- Scales from 2 to 8 threads
- Execution time comparison against serial version

###  Question (b)
- Implements `gaussian_blur_separate_parallel(...)`
- Multi-stage processing:
  1. Max pixel values per channel (with atomics)
  2. Pixel normalization
  3. Horizontal blur
  4. Vertical blur
- Synchronization with `std::barrier`
- Thread-safe and structured execution

---

##  Performance Results

### Blur with 2 Threads
| Run | Serial (ms) | Parallel (2 threads) (ms) |
|-----|-------------|----------------------------|
| 1   | 78036       | 39716                      |
| 2   | 76303       | 38734                      |
| 3   | 75895       | 43291                      |
| 4   | 76287       | 39877                      |

**Average**:  
- Serial: 76,630 ms  
- Parallel (2 threads): 40,354 ms

---

### Blur with 4 Threads

**Average**: 23,289 ms

---

### Blur with 8 Threads

**Average**: 17,009 ms

---

### Question (b) – Multistage Blur (4 Threads)

| Run | Serial (ms) | Parallel (4 threads) (ms) |
|-----|-------------|----------------------------|
| 1   | 10279       | 3594                       |
| 2   | 11806       | 4398                       |
| 3   | 10412       | 3743                       |
| 4   | 10243       | 3639                       |

**Averages**:
- Serial: 10,685 ms  
- Parallel: 3,843.5 ms

---

##  Conclusions

- Parallelization offers **significant performance improvements**, especially from serial to 4-thread execution.
- Beyond 4 threads, gains taper off due to overhead.
- Structured multithreading (using `std::barrier`) makes complex pipelines efficient and thread-safe.
- Ideal for image processing workloads that can be easily partitioned.

---

##  Tools & Dependencies

- **C++17**
- `std::thread`, `std::barrier`, `std::atomic`

---


---

