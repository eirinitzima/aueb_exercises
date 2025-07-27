__kernel void gaussian_blur_axis(__global const uchar* input,
                                __global uchar* output,
                                __constant float* weights, 
                                const int width, 
                                const int height, 
                                const int axis, 
                                const int radius) {

    //getting pixels pos
    int x = get_global_id(0);

    int y = get_global_id(1);
    
    //checks the limits of the image
    if (x >= width || y >= height) return;

    //for every channel 
    for (int channel = 0; channel < 4; channel++) {
        float sum = 0.0f;
        float weight_sum = 0.0f;
        
        // Apply gaussian blur to 
        for (int offset = -radius; offset <= radius; offset++) {

            int sample_x = x + (axis == 0 ? offset : 0); // Horizontal blur affects x
            int sample_y = y + (axis == 1 ? offset : 0); // Vertical blur affects y

            sample_x = clamp(sample_x, 0, width - 1);
            sample_y = clamp(sample_y, 0, height - 1);

            int sample_index = 4 * (sample_y * width + sample_x);

            // Read pixel value from the input image for the current channel
            float pixel = (float)(input[sample_index + channel]);

            float w = weights[offset + radius];

            sum += pixel * w;
            weight_sum += w;
        }

        int output_index = 4 * (y * width + x);
        output[output_index + channel] = (uchar)clamp(sum / weight_sum, 0.0f, 255.0f);
    }
}
