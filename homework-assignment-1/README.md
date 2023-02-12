## Homework 1: Aliasing and Resampling ##

The purpose of this homework assignment is to understand Sampling and Filtering of signals
spatially and temporally. The homework is divided into two parts covering Sampling and
Aliasing in [1. Spatial](#part-1-spatial-resampling-and-aliasing) and [2. Temporal](#part-2-temporal-aliasing) domains.

Refer to the [homework specs.](Assignment%201%20-%20Description.pdf) for exact details of the assignment.

### Part 1. Spatial Resampling and Aliasing

Here two images are generated and displayed side by side. The first image is the original
generated image of size 512x512 and the second one is a resampled version of the image scaled
by a factor of ```s```. The task is to observe the scaled image with and without anti-aliasing.

The program takes in the following parameters:
```
n - Number of lines to draw on the otherwise white image.
    (The lines are arraged like spokes in a wheel with each line seperated by 360/n degrees) 
s - Scaling factor (between 0 and 1)
a - A boolean value (0 or 1) indicating whether to perform anti-aliasing.
```

#### The results for the experiments described in the homework specs. can be found here:
- [Experimental Analysis](https://docs.google.com/document/d/1Kwif_kYDHaFsFIYQtV6ImcbEmn2T79_qLTCSGNEbfuM/edit?usp=sharing)
- [Output from the experimentation](https://drive.google.com/drive/folders/1H_t-mGbfAKybV21AEpGd2UfQoU4a7v7r?usp=sharing)

#### Demo run for parameters (n=64 [lines], s=0.5 ([scale factor], a=0 [no anti-aliasing])

![demo without anti-aliasing](assets/sample-without-anti-aliasing.png)

#### Demo run for parameters (n=64 [lines], s=0.5 ([scale factor], a=1 [anti-aliasing])

![demo with anti-aliasing](assets/sample-with-anti-aliasing.png)

### Part 2. Temporal Aliasing

Here two videos are rendered and displayed side by side. The first video is the original
generated video of size 512x512 and the second one is a sampled version of the video sampled at a rate
of ```fps```. The task is to observe the scaled video and find out a relation between the observed speed of rotation,
actual speed of rotation and the sampling rate (fps).

This experiment demonstrates the wagon wheel effect.


The program takes in the following parameters:
```
n   - Number of lines to draw on the otherwise white image.
      (The lines are arraged like spokes in a wheel with each line seperated by 360/n degrees) 
s   - Speed of rotation (rotations per second) of original video.
fps - Frames per second (sampling rate) to render the second video.
```

#### The results for the experiments described in the homework specs. can be found here:
- [Experimental Analysis](https://docs.google.com/document/d/1Kwif_kYDHaFsFIYQtV6ImcbEmn2T79_qLTCSGNEbfuM/edit?usp=sharing)
- [Output from the experimentation](https://drive.google.com/drive/folders/1H_t-mGbfAKybV21AEpGd2UfQoU4a7v7r?usp=sharing)

#### Demo run for parameters (n=64 [lines], s=10.0 ([speed of rotation], fps=25.0 [sampling rate])
- [Link](https://drive.google.com/file/d/1TymUFETGow4z8nkHyem2C3XJEcGVbqaS/view?usp=share_link)

#### Demo run for parameters (n=64 [lines], s=10.0 ([speed of rotation], fps=16.0 [sampling rate])
- [Link](https://drive.google.com/file/d/1OprFDlGqCp5k3A9Go_KNLlIgnKiN4ZBJ/view?usp=share_link)

#### Demo run for parameters (n=64 [lines], s=10.0 ([speed of rotation], fps=10.0 [sampling rate])
- [Link](https://drive.google.com/file/d/134SzwqtgCSKa8jtTAha1T8LjH_xNwejq/view?usp=share_link)

#### Demo run for parameters (n=64 [lines], s=10.0 ([speed of rotation], fps=8.0 [sampling rate])
- [Link](https://drive.google.com/file/d/1gkt9sAmiP_IwZ39ZVz6lhlezNq2Gq_au/view?usp=share_link)

#### Demo run for parameters (n=64 [lines], s=10.0 ([speed of rotation], fps=6.0 [sampling rate])
- [Link](https://drive.google.com/file/d/1w32za05cGwFVc1vrLyVfu4y3WMGK_qnq/view?usp=share_link)

Additional references
- https://www.mekanizmalar.com/wagon-wheel-effect.html
- https://blog.prosig.com/2010/06/29/aliasing-orders-and-wagon-wheels/
- https://jackschaedler.github.io/circles-sines-signals/sampling4.html
- https://www.technomaxme.com/nyquist-theory-for-rotational-order-analysis/
- https://visualize-it.github.io/stroboscopic_effect/simulation.html
