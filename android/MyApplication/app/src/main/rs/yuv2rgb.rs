/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#pragma version(1)
#pragma rs java_package_name(com.newind.mycamera2)
#pragma rs_fp_relaxed
#include "rs_debug.rsh"

uint32_t width,height;
uint32_t dst_width,dst_height;
rs_allocation gCurrentFrame;
//rs_allocation gResultFrame;

uchar4 __attribute__((kernel)) yuv2rgbFrames(uchar4 in, uint32_t x, uint32_t y) {
    uchar4 out = in;
    //out.r = *(uchar*)rsGetElementAt(gCurrentFrame,(height - x * height / dst_width) * width + y * width / dst_height);
    //out.r = *(uchar*)rsGetElementAt(gCurrentFrame,(height - x) * width + y);
    out.r = *(uchar*)rsGetElementAt(gCurrentFrame,x * width + y * height / dst_height);
    out.g = 0;//x & 0xFF;//in.b;
    out.b = 0;//y & 0xFF;
    return out;
}
