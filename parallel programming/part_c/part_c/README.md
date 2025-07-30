#  Gaussian Blur with OpenCL

This project implements the **Gaussian Blur** filter on images using **OpenCL**, aiming to accelerate image processing by offloading computations to the GPU. The implementation compares serial and parallel execution and applies optimization techniques such as **separable blur** and **work-group tuning**.

---

##  Project Summary

- Language: **C++ + OpenCL**
- Task: Apply Gaussian blur (horizontal + vertical) to an image
- Method: Separable Gaussian blur
- Device: GPU (via OpenCL)
- Goal: Compare performance vs. serial CPU execution

---

##  How It Works

###  Separable Gaussian Blur

Instead of applying a 2D kernel directly, the blur is split into **two passes**:

1. **Horizontal pass** (`axis = 0`)
2. **Vertical pass** (`axis = 1`)

This reduces computational complexity from O(k²) to O(2k) and improves GPU cache usage.

###  OpenCL Kernel Logic

- Each `work-item` processes **one pixel**
- For each pixel and for every **R, G, B, A** channel:
  - Read neighboring pixels along the given axis
  - Compute weighted average using **Gaussian weights**
- Gaussian weights are **precomputed on host** and passed via buffer to the kernel

---

##  Host-Side Pipeline

1. Load image using `stb_image`
2. Compute Gaussian weights on CPU
3. Create OpenCL buffers:
   - Input image
   - Output image
   - Gaussian weights
4. Launch kernel twice (once per axis)
5. Retrieve result from GPU
6. Save final image using `stbi_write_jpg`

---

## Performance Experiments

### Tuning `local_work_size`

| local_work_size | Avg Time (ms) |
|------------------|---------------|
| Default          | 686           |
| (16, 16)         | 761           |
| (8, 16)          | 500           |
| (16, 8)          | 465           |
| ✅ (32, 32)      | **449.75**     |

Best performance was achieved with **(32, 32)**, making optimal use of GPU resources.

---

### Serial vs Parallel Comparison

| Execution Type       | Avg Time (ms) | Speedup |
|----------------------|---------------|---------|
| Serial (CPU)         | 2175          | 1×      |
| Parallel (OpenCL)    | 449.75        | **~4.8×** |

---
