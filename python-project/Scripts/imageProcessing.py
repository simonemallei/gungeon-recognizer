import random
import numpy as np
import matplotlib
from PIL import Image, ImageFilter, ImageEnhance


def rotateLeft(item_im):
    item_arr = np.array(item_im)
    new_item_arr = np.zeros((item_arr.shape[1], item_arr.shape[0], item_arr.shape[2]))
    for i in range(item_arr.shape[0]):
        new_item_arr[:, i, :] = item_arr[i, :, :][::-1]
    rotate_im = Image.fromarray(new_item_arr.astype('uint8'))
    return rotate_im


def createSample(item_im, room_im, special_type = 0):
    ''' Implementation of creating method for a single sample, given
        the item's, the room's and the container's images. 

    Parameters
    ----------
    item_im : object (PIL.Image)
        Sample's item image to use.

    room_im : object (PIL.Image)
        Sample's room image to use.

    special_type : int, default: 0
        special_type == 0 if the room's image is not special
        special_type == 1 if the room's image is "X special"
        special_type == 2 if the room's image is "Y special"
        special_type == 3 if the room's image is "XY special"
        special_type == 4 if the room's image is "XY NPC Shop"
        special_type == 5 if the room's image is "XY Fixed with pedestal"
        special_type == 6 if the room's image is "XY Fixed"
        special_type == 7 if the room's image is "Rainbow Run"

    Returns
    -------
    im_arr : array-like with shape (width_img, height_img, n_channels)
        Array containing RGB (range [1, 255]) values of the sample obtained.

    '''
    # initialization 
    width_ds, height_ds, num_channels = 32, 32, 3
    width_fl, height_fl = room_im.width, room_im.height
    sample_im = room_im.copy()

    # defining edge length of the sample's image
    start_edge = [80, 100, 110, 120, 130, 140, 150]
    end_edge = [100, 110, 120, 130, 140, 150, 200]
    edge_len_ind = random.randrange(len(start_edge))
    edge_length = random.randrange(start_edge[edge_len_ind], end_edge[edge_len_ind])
    edge_length = int(edge_length / 100 * max(item_im.width, item_im.height)) 
    if edge_length > min(width_fl, height_fl) - 5:
        edge_length = min(width_fl, height_fl) - 5

    # defining item's shift on width and height
    width_shift, height_shift = random.randrange(-3, 4, 1), random.randrange(-3, 4, 1)
    # defining (x, y)-coordinate of the "starting" pixel:
    if special_type == 0: # not special
        # 1. x in range [width_fl/5, width_fl*4/5],
        # 2. y in range [height_fl/3.5, height_fl*2.5/3.5]
        x_begin = random.randrange((width_fl-edge_length)//3)+width_fl//5
        y_begin = random.randrange(int((height_fl-edge_length)/1.5))+int(height_fl/3.5)
    elif special_type == 1: # X special: variable X, fixed Y
        # 1. x in range [width_fl/4, width_fl*3/4]
        x_begin = random.randrange((width_fl-edge_length)//2)+width_fl//4
        y_begin = (height_fl-edge_length)//2 + random.randrange(-4, 5)
    elif special_type == 2: # Y special: variable Y, fixed X
        # 1. y in range [height_fl/4, height_fl*3/4]
        x_begin = (width_fl-edge_length)//2 + random.randrange(-4, 5)
        y_begin = random.randrange((height_fl-edge_length)//2)+height_fl//4
    elif special_type == 3: # XY special: variable XY
        # 1. x in range [width_fl/4, width_fl*3/4],
        # 2. y in range [height_fl/4, height_fl*3/4]
        x_begin = random.randrange((width_fl-edge_length)//2)+width_fl//4
        y_begin = random.randrange((height_fl-edge_length)//2)+height_fl//4
    elif special_type == 4: # XY NPC Shop: variable XY
        # 1. x in range [width_fl/3, width_fl*2/3],
        # 2. y in range [height_fl/3, height_fl*2/3]
        x_begin = random.randrange((width_fl-edge_length)//3)+width_fl//3
        y_begin = random.randrange((height_fl-edge_length)//3)+height_fl//3
    elif special_type == 5: # XY Fixed with pedestal
        x_begin = (width_fl-edge_length)//2
        y_begin = (height_fl-edge_length-item_im.height)//2 + random.randrange(-2, 3)
    elif special_type == 6: # XY Fixed:
        x_begin = (width_fl-edge_length)//2 + random.randrange(-2, 3)
        y_begin = (height_fl-edge_length)//2 + random.randrange(-2, 3)
    elif special_type == 7: # Rainbow Run:
        # (x, y) is at most distant 60 pixels from the center
        distance = random.randrange(60)
        degree = random.randrange(360)
        radius = (degree / 360) * 2 * np.pi
        x_rad = int(np.cos(radius) * distance)
        y_rad = int(np.sin(radius) * distance)
        x_begin = (width_fl-edge_length)//2 + x_rad + random.randrange(-2, 3)
        y_begin = (height_fl-edge_length)//2 + y_rad + random.randrange(-2, 3)
    # pasting the item in the room
    sample_im.paste(item_im,
                    (x_begin+(edge_length-item_im.width)//2,
                     y_begin+(edge_length-item_im.height)//2),
                    item_im)

    standard_image = random.randrange(10)
    # limiting the changes applied(10% possibility)
    if standard_image == 1:
        # applying brightness change (50% possibility)
        change_brightness = random.randrange(2)
        if change_brightness == 1:
            enhancer = ImageEnhance.Brightness(sample_im)
            start_enh = [0.05, 0.1, 0.2, 0.3, 0.5, 0.8, 1.3, 2, 2.5]
            end_enh = [0.1, 0.2, 0.3, 0.5, 0.8, 1.3, 2, 2.5, 5]
            num_enh = [50, 100, 100, 200, 300, 50, 70, 50, 250]
            enh_ind = random.randrange(len(num_enh))
            factor = start_enh[enh_ind]+random.randrange(num_enh[enh_ind]) * (end_enh[enh_ind]-start_enh[enh_ind]) / num_enh[enh_ind]
            sample_im = enhancer.enhance(factor)

        # applying contrast change (50% possibility)
        change_contrast = random.randrange(2)
        if change_contrast == 1:
            enhancer = ImageEnhance.Contrast(sample_im)
            start_enh = [0.05, 0.1, 0.2, 0.3, 0.5, 0.8, 1.3, 2, 2.5]
            end_enh = [0.1, 0.2, 0.3, 0.5, 0.8, 1.3, 2, 2.5, 5]
            num_enh = [50, 100, 100, 200, 300, 50, 70, 50, 250]
            enh_ind = random.randrange(len(num_enh))
            factor = start_enh[enh_ind]+random.randrange(num_enh[enh_ind]) * (end_enh[enh_ind]-start_enh[enh_ind]) / num_enh[enh_ind]
            sample_im = enhancer.enhance(factor)

        # cropping the image from the sample obtained based on
        # edge length and random stretching obtained
        sample_im = sample_im.crop((x_begin,
                                    y_begin,
                                    x_begin+edge_length,
                                    y_begin+edge_length))

    else:
        # random rotation (angle_rot in degrees counter clockwise)
        rotation = random.randrange(2)
        if rotation == 1:
            center = (x_begin+edge_length//2, y_begin+edge_length//2)    
            angle_rot = random.randrange(-4, 5)
            sample_im = sample_im.rotate(angle_rot, center=center)

        # applying brightness change (50% possibility)
        change_brightness = random.randrange(2)
        if change_brightness == 1:
            enhancer = ImageEnhance.Brightness(sample_im)
            start_enh = [0.05, 0.1, 0.2, 0.3, 0.5, 0.8, 1.3, 2, 2.5]
            end_enh = [0.1, 0.2, 0.3, 0.5, 0.8, 1.3, 2, 2.5, 5]
            num_enh = [50, 100, 100, 200, 300, 50, 70, 50, 250]
            enh_ind = random.randrange(len(num_enh))
            factor = start_enh[enh_ind]+random.randrange(num_enh[enh_ind]) * (end_enh[enh_ind]-start_enh[enh_ind]) / num_enh[enh_ind]
            sample_im = enhancer.enhance(factor)

        # applying contrast change (50% possibility)
        change_contrast = random.randrange(2)
        if change_contrast == 1:
            enhancer = ImageEnhance.Contrast(sample_im)
            start_enh = [0.05, 0.1, 0.2, 0.3, 0.5, 0.8, 1.3, 2, 2.5]
            end_enh = [0.1, 0.2, 0.3, 0.5, 0.8, 1.3, 2, 2.5, 5]
            num_enh = [50, 100, 100, 200, 300, 50, 70, 50, 250]
            enh_ind = random.randrange(len(num_enh))
            factor = start_enh[enh_ind]+random.randrange(num_enh[enh_ind]) * (end_enh[enh_ind]-start_enh[enh_ind]) / num_enh[enh_ind]
            sample_im = enhancer.enhance(factor)

        # applying sharpness change (50% possibility)
        change_sharpness = random.randrange(2)
        if change_sharpness == 1:
            enhancer = ImageEnhance.Sharpness(sample_im)
            start_enh = [0.05, 0.1, 0.2, 0.3, 0.5, 0.8, 1.3, 2, 2.5]
            end_enh = [0.1, 0.2, 0.3, 0.5, 0.8, 1.3, 2, 2.5, 5]
            num_enh = [50, 100, 100, 200, 300, 50, 70, 50, 250]
            enh_ind = random.randrange(len(num_enh))
            factor = start_enh[enh_ind]+random.randrange(num_enh[enh_ind]) * (end_enh[enh_ind]-start_enh[enh_ind]) / num_enh[enh_ind]
            sample_im = enhancer.enhance(factor)

        # applying color change (50% possibility)
        change_color = random.randrange(2)
        if change_color == 1:
            enhancer = ImageEnhance.Color(sample_im)
            start_enh = [0.05, 0.1, 0.2, 0.3, 0.5, 0.8, 1.3, 2, 2.5]
            end_enh = [0.1, 0.2, 0.3, 0.5, 0.8, 1.3, 2, 2.5, 5]
            num_enh = [50, 100, 100, 200, 300, 50, 70, 50, 250]
            enh_ind = random.randrange(len(num_enh))
            factor = start_enh[enh_ind]+random.randrange(num_enh[enh_ind]) * (end_enh[enh_ind]-start_enh[enh_ind]) / num_enh[enh_ind]
            sample_im = enhancer.enhance(factor)    

        # random stretching (50% possibility: 25% on X, 25% on Y)
        x_stretch, y_stretch = 0, 0
        stretch_ind = random.randrange(4)
        if stretch_ind == 2:
            x_stretch = random.randrange(-4, 5)
        elif stretch_ind == 3:
            y_stretch = random.randrange(-4, 5)

        x_begin_new = x_begin + width_shift
        y_begin_new = y_begin + height_shift

        # cropping the image from the sample obtained based on
        # edge length and random stretching obtained
        sample_im = sample_im.crop((x_begin_new,
                                    y_begin_new,
                                    x_begin_new+edge_length,
                                    y_begin_new+edge_length))

    # resizing into 32 x 32 image
    sample_im = sample_im.resize((width_ds, height_ds)).convert("RGB")

    im_arr = np.array(sample_im)
    im_arr = np.uint8(np.round(im_arr))

    return im_arr


def histEqualizeImage(X):
    ''' Implementation of Histogram Equalization algorithm on a RGB
        image array (converting RGB values into HSV and applying
        Histogram Equalization method on V values), considering each
        image separately.

    Parameters
    ----------
    X : array-like with shape (n_samples, width_img, height_img, n_channels)
        The input images array that contains all RGB values (range [0, 1]) for
        each pixel.

    Returns
    -------
    X : array-like with shape (n_samples, width_img, height_img, n_channels)
        The images array with new RGB values (range [0,1]) obtained
        by the method.

    '''
    hsv_arr = matplotlib.colors.rgb_to_hsv(X)
    # reshaping V (from HSV) values in order to have (n_samples, n_values) shape
    V = hsv_arr[:, :, :, 2].reshape((hsv_arr.shape[0], hsv_arr.shape[1] * hsv_arr.shape[2]))

    # applying Histogram Equalization on V
    V = histEqualize(V)

    # memorizing new values of V in the original dataset and converting
    # it back in RGB
    hsv_arr[:, :, :, 2] = V.reshape(hsv_arr.shape[:3])
    X = matplotlib.colors.hsv_to_rgb(hsv_arr)
    X = np.round(X * 255) / 255
    return X


def histEqualize(X):
    ''' Implementation of Histogram Equalization algorithm, considering
        each sample separately.

    Parameters
    ----------
    X : array-like with shape (n_samples, n_values)
        The input values to apply Histogram Equalization.

    Returns
    -------
    X_new : array-like with shape (n_samples, n_values)
        The new values obtained applying the method. 

    '''
    # sorting the array to equalize
    X_new = np.sort(X, axis=1)
    X_arg = np.argsort(np.argsort(X, axis=1))

    # computing cumulative distribution for each sample
    min_cdf = np.full(X_new.shape[0], X_new.shape[1])
    dif_cdf = np.zeros(X_new.shape)
    error_ind = np.full(X_new.shape[0], -1)
    for i in range(X_new.shape[1]):
        if i != 0:
            min_cdf = np.where(X_new[:, i] != X_new[:, i-1], np.minimum(min_cdf, i), min_cdf)
        if i != X_new.shape[1] - 1:
            next_ind = np.full(error_ind.shape, i+1)
            dif_cdf[:, i] = np.where(X_new[:, i] != X_new[:, i+1], next_ind, error_ind)
        else:
            dif_cdf[:, i] = X_new.shape[1]
    dif_cdf = np.flip(dif_cdf, 1)
    for i in range(X_new.shape[1]):
        if i != 0:
            dif_cdf[:, i] = np.where(dif_cdf[:, i] != -1, dif_cdf[:, i], dif_cdf[:, i-1])
    dif_cdf = np.flip(dif_cdf, 1)
    X_new = np.take_along_axis(dif_cdf, X_arg, axis=1)

    # computing the histogram equalization using original positions
    X_new = np.where(X_new.shape[1] - min_cdf.reshape(X_new.shape[0], 1) != 0,
                     ((X_new - min_cdf.reshape(X_new.shape[0], 1)) /
                      (X_new.shape[1] - min_cdf.reshape(X_new.shape[0], 1))),
                     1)
    
    return X_new


def preprocessing(X):
    ''' Implementation of the following preprocessing steps for each image:
        - Histogram Equalization on V values (from RGB images converted into HSV)
        for a half of the dataset.
        - Adding Gaussian Noise on V Values (from RGB images converted into HSV).

    Parameters
    ----------
    X : array-like with shape (n_samples, width_img, height_img, n_channels)
        The input images array that contains all RGB values (range [0, 255]) for
        each pixel.

    Returns
    -------
    X : array-like with shape (n_samples, width_img, height_img, n_channels)
        The images array with new RGB values (range [0, 255]) obtained by the
        histogram equalization and the Gaussian Noise.

    '''
    # from range [0, 255] RGB to range [0, 1] RGB
    X = X / 255
        
    # adding Gaussian Noise on V (from HSV) values for all the samples
    # with mu = 0, std = random(0 (no noise), 0.025, 0.05, 0.075)
    X = matplotlib.colors.rgb_to_hsv(X)
    gaussian_std = [0, 0, 0.05, 0.1, 0.15, 0.2, 0.25] 
    rng = np.random.default_rng()
    gauss = np.zeros(X.shape[:3])
    std_random_ind = rng.integers(low=0, high=len(gaussian_std), size=X.shape[:3])
    mu_list = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
               0.025, 0.05, 0.1, 0.15, -0.025, -0.05, -0.1, -0.15]
    mu_random_ind = random.randrange(len(mu_list))
    mu = mu_list[mu_random_ind]
    for std_ind in range(len(gaussian_std)):
        curr_gauss = np.random.normal(mu, gaussian_std[std_ind], X.shape[:3])
        gauss = np.where(std_ind == std_random_ind, curr_gauss, gauss)
    X[:, :, :, 2] = X[:, :, :, 2] + gauss
    X = np.where(X > 1.0, 1.0, X)
    X = np.where(X < 0.0, 0.0, X)
    X = matplotlib.colors.hsv_to_rgb(X)

    # applying Histogram Equalization on V (from HSV) values only
    # for the first half of the dataset's samples
    X[:X.shape[0]//2] = histEqualizeImage(X[:X.shape[0]//2])

    # from range [0, 1] RGB to range [0, 255] RGB
    X = np.round(X * 255)

    return X
