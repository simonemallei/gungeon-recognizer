# gungeon-recognizer/recognition-model/2-1.1

The directory contains the .h5 model's file used in "Unofficial Gungeon Item Recognizer" - Version 2 (1.1).

The model has been created using tensorflow.keras module and, in order to use it in the Android app, it has been converted into a .tflite model (you can find the corresponding script in "gungeon-recognizer/recognition-model/scripts/convert.py").

The model's structure is composed by the following layers:
- Conv2D(64, 7, activation = "relu", padding = "same", input_shape = \[32, 32, 3\]),
- MaxPooling2D(2),
- BatchNormalization(),
- Conv2D(128, 5, activation = "relu", padding = "same"),
- Conv2D(128, 5, activation = "relu", padding = "same"),
- MaxPooling2D(2),
- BatchNormalization(),
- Conv2D(256, 3, activation = "relu", padding = "same"),
- Conv2D(256, 3, activation = "relu", padding = "same"),
- MaxPooling2D(2),
- BatchNormalization(),
- Flatten(),
- BatchNormalization(),
- Dense(512, activation = "relu"),
- Dropout(0.5),
- BatchNormalization(),
- Dense(256, activation = "relu"),
- Dropout(0.5),
- BatchNormalization(),
- Dense(128, activation = "relu"),
- Dropout(0.5),
- Dense(n_classes, activation = "softmax"),

where n_classes = 509 (number of items and guns the model can recognize).

It is compiled using:
- optimizer = Adam(learning_rate=0.0001, beta_1=0.9, beta_2=0.999),
- loss = "categorical_crossentropy", 
- metrics = \["accuracy"\].

Recommendations:
- The input shape \[32, 32, 3\] refers to a 32x32 RGB image (with values in range \[0; 1\])
- The image's indexing: the (0, 0) pixel refers to the top-left and (y, x) pixel refers to the y-th row and x-th column, hence we'll have:
  - (0, 0): top-left pixel,
  - (31, 0): bottom-left pixel,
  - (0, 31): top-right pixel,
  - (31, 31): bottom-right pixel.
   
- Prior to use the model in order to recognize an item, it should be better to perform a histogram equalization on V values of the corresponding HSV image. 
  
  Hence you ought to:
  - Convert the image from RGB to HSV,
  - Perform Histogram Equalization on V,
  - Convert it back from HSV to RGB.
- You should refer to the app's IDs (you have to increment once the index obtained by the model): for example, if the model gives you 32 as its prediction, the corresponding ID would be 33.