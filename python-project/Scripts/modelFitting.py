import json
import random
import numpy as np
from tensorflow import keras
from PIL import Image
import time
import os
from imageProcessing import createSample, preprocessing, rotateLeft
from sklearn.metrics import top_k_accuracy_score


def createDataset(size, item_imgs, floor_imgs, type_floor):
    ''' Returns a dataset of {size} samples images, given {item_imgs},
        {floor_imgs} and {container_imgs}

    Parameters
    ----------
    size : int
        The number of the samples that need to be created.
    item_imgs : list of object (PIL.Image)
        The items' images to use in order to put the samples' items.
    floor_imgs : list of list of object (PIL.Image)
        The floors' images (for each floor there's a list of rooms' images)
        to use in order to put the samples' rooms.
    type_floor:  list of int
        type_floor[i] contains type of images in floor_imgs[i]

    Returns
    -------
    X : array-like with shape (size, width_im, height_im, num_channels)
        Array containing, for each sample, the RGB values in the range [0, 1],
        X[i][x][y] contains the RGB values of (x,y)-pixel of the i-th sample.
    y : array-like with shape (size, n_classes)
        Ground-truth for each sample as hot-one vector,
        y[x][k] == 1.0 if x-th sample is assigned to k-th class.

    '''
    X, y = [], []
    
    for i in range(size):
        # selecting the i-th item to use
        item_ind = i % len(item_imgs)
        item_im = item_imgs[item_ind]
        rotate = random.randrange(2)
        # left rotation (90 degree) of Casey (50% possibility)
        if item_ind == 278 and rotate == 1: 
            item_im = rotateLeft(item_im)
        
        # selecting the i-th room to use
        floor_ind = random.randrange(len(floor_imgs))
        room_ind = random.randrange(len(floor_imgs[floor_ind]))
        room_im = floor_imgs[floor_ind][room_ind]
        
        # setting i-th sample's data
        nois_arr = createSample(item_im, room_im, i, type_floor[floor_ind])
        X.append(nois_arr)
        hot_one_vec = np.zeros(len(item_imgs))
        hot_one_vec[item_ind] = 1
        y.append(np.array(hot_one_vec))

    X, y = np.array(X), np.array(y)
    
    # applying preprocessing methods and casually permutating the dataset
    X = preprocessing(X)
    curr_perm = np.random.permutation(X.shape[0])
    X = X[curr_perm]
    y = y[curr_perm]
    X = (X / 255).astype('float32')
    
    return X, y


def createModel(n_classes = 10):
    ''' Classification model's creation and compilation
        with {n_classes} classes.

    Parameters
    ----------
    n_classes : int, default = 10
        Number of model's original classes.

    Returns
    -------
    model : object
        Compiled classification model.

    '''
    model = keras.models.Sequential([
        keras.layers.Conv2D(64, 7, activation="relu", padding="same", input_shape=[32, 32, 3]),
        keras.layers.MaxPooling2D(2),
        keras.layers.BatchNormalization(),
        keras.layers.Conv2D(128, 5, activation="relu", padding="same"),
        keras.layers.Conv2D(128, 5, activation="relu", padding="same"),
        keras.layers.MaxPooling2D(2),
        keras.layers.BatchNormalization(),
        keras.layers.Conv2D(256, 3, activation="relu", padding="same"),
        keras.layers.Conv2D(256, 3, activation="relu", padding="same"),
        keras.layers.MaxPooling2D(2),
        keras.layers.BatchNormalization(),
        keras.layers.Flatten(),
        keras.layers.BatchNormalization(),
        keras.layers.Dense(512, activation="relu"),
        keras.layers.Dropout(0.5),
        keras.layers.BatchNormalization(),
        keras.layers.Dense(256, activation="relu"),
        keras.layers.Dropout(0.5),
        keras.layers.BatchNormalization(),
        keras.layers.Dense(128, activation="relu"),
        keras.layers.Dropout(0.5),
        keras.layers.Dense(n_classes, activation="softmax")
        ])
    
    curr_optimizer = keras.optimizers.Adam(learning_rate=0.0001, beta_1=0.9, beta_2=0.999)
    model.compile(loss="categorical_crossentropy", optimizer=curr_optimizer, metrics=["accuracy"])
    
    return model


def fit_recognizer(model_info):
    ''' Implementation of a trained item's classification model in order
        to use it for a "Enter The Gungeon".

    Parameters
    ----------
    model_info : dict
        Dictionary containing all the infos needed by the script.

    Returns
    -------
    model : object
        Trained classification model based on {model_info}.

    '''
    print(f'Creating Gungeon model...')


    # loading items' images
    item_f = open(f'../Data/Items.json', 'r')
    items = json.loads(item_f.read())
    item_imgs = []
    for item in items:
        item_imgs.append(Image.open(f'../Images/Items/{item["id"]}.png'))
    
    # loading rooms' images
    floor_f = open('../Data/Floors.json', 'r')
    (floors, type_floor) = json.loads(floor_f.read())
    floor_imgs = []
    for floor in floors:
        room_f = [(floor + f) for f in os.listdir(floor) if os.path.isfile(os.path.join(floor, f))]
        curr_floor = []
        for curr_room_f in room_f:
            curr_floor.append(Image.open(curr_room_f))
        floor_imgs.append(curr_floor)

    # loading / creating item's classification model
    if model_info['create_model'] == True:
        model = createModel(n_classes = len(items))
    else:
        model = keras.models.load_model(f'{model_info["path"]}GungeonModel.h5')

    # creating validation and testing sets
    start = time.perf_counter()
    X_valid, y_valid = createDataset(model_info['valid_size'],
                                     item_imgs,
                                     floor_imgs,
                                     type_floor)
    X_test, y_test = createDataset(model_info['test_size'],
                                   item_imgs,
                                   floor_imgs,
                                   type_floor)
    print(f'Validation and testing sets created in : {np.round(time.perf_counter() - start, 3)} sec')

    # fitting {num_fit} datasets with {num_epochs} epochs
    for curr_fit in range(model_info['num_fit']):
        # evaluating top-10 accuracy on validation set pre-fitting
        y_valid_class = np.argmax(y_valid, axis=1)
        y_pred = model.predict(X_valid.astype('float64'))
        acc = top_k_accuracy_score(y_valid_class, y_pred, k=model_info['len_result'])
        print(f"\nValidation - Accuracy (top {model_info['len_result']}) pre-fitting #{curr_fit + 1}: {np.round(acc, 5)}")
    
        # creating training set
        print(f'Gungeon - Creating training set #{curr_fit + 1}')
        start = time.perf_counter()
        X_train, y_train = createDataset(model_info['train_size'],
                                         item_imgs,
                                         floor_imgs,
                                         type_floor)
        print(f'Training set #{curr_fit + 1} created in : {np.round(time.perf_counter() - start, 3)} sec')

        # fitting the current set
        model.fit(X_train.astype('float64'),y_train,epochs=model_info['num_epochs'],validation_data=(X_valid.astype('float64'), y_valid),verbose=2)

        # evaluating top-10 accuracy on validation set post-fitting
        y_valid_class = np.argmax(y_valid, axis=1)
        y_pred = model.predict(X_valid.astype('float64'))
        acc = top_k_accuracy_score(y_valid_class, y_pred, k=model_info['len_result'])
        print(f"Validation - Accuracy (top {model_info['len_result']}) post-fitting #{curr_fit + 1}: {np.round(acc, 5)}\n")

    # evaluating top-10 accuracy on testing set
    y_test_class = np.argmax(y_test, axis=1)
    y_pred = model.predict(X_test.astype('float64'))
    acc = top_k_accuracy_score(y_test_class, y_pred, k=model_info['len_result'])
    print(f"Testing - Accuracy (top {model_info['len_result']}): {np.round(acc, 5)}")

    return model
