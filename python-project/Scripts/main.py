import tensorflow as tf
from modelFitting import fit_recognizer

def main():
    ''' Creates a item's classification
        Keras model and converts it into a TFLite model.

    '''
    
    model_info = {'create_model': True,
                  'len_result' : 10,
                  'num_epochs' : 3,
                  'num_fit' : 6,
                  'path' : '../AppModel/',
                  'train_size' : 70000,
                  'test_size': 20000,
                  'valid_size': 20000
                  }

    model = fit_recognizer(model_info)

    model.save(f'{model_info["path"]}GungeonModel.h5')    
        # converting the Keras model obtained into the corresponding TFLite model
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()
    out_tflite = open(f"{model_info['path']}GungeonModel.tflite",
                      "wb")
    out_tflite.write(tflite_model)

    
if __name__ == '__main__':
    main()
