#!/usr/bin/python
# -*- coding: utf-8 -*-

''' convert.py:

    Provides a method that performs a conversion from
    a .h5 file to a .tflite one.
'''

import tensorflow as tf
from tensorflow import keras

__author__ = "Simone Mallei"

def convert(model_path):
    ''' Converts a .h5 file model to a .tflite one.

        Parameters
        ----------
        model_path: string
            The path to the directory containing the .h5 file

    '''
    # loading the model 
    model = keras.models.load_model(f"{model_path}GungeonModel.h5")
    # converting the Keras model obtained into the corresponding TFLite model
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()
    out_tflite = open(f"{model_path}GungeonModel.tflite",
                      "wb")
    out_tflite.write(tflite_model)
    out_tflite.close()

def main():
    ''' Instance of a conversion (using Version 1 (1.0) path).
        
    '''
    model_path = "../1-1.0/"
    convert(model_path)

if __name__ == "__main__":
    main()
