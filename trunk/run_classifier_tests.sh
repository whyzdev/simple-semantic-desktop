#!/bin/sh
java ssd.TrainModel /local/s/datasets/20news/train /local/s/datasets/20news/test /local/s/datasets/20news/test-results /local/s/datasets/20news/models
java ssd.TestModel /local/s/20news/datasets/test /local/s/20news/datasets/test-results /local/s/datasets/20news/models > /local/s/datasets/20news/results.txt

java ssd.TrainModel /local/s/datasets/cornell/train /local/s/datasets/cornell/test /local/s/datasets/cornell/test-results /local/s/datasets/cornell/models
java ssd.TestModel /local/s/cornell/datasets/test /local/s/cornell/datasets/test-results /local/s/datasets/cornell/models > /local/s/datasets/cornell/results.txt

java ssd.TrainModel /local/s/datasets/texas/train /local/s/datasets/texas/test /local/s/datasets/texas/test-results /local/s/datasets/texas/models
java ssd.TestModel /local/s/texas/datasets/test /local/s/texas/datasets/test-results /local/s/datasets/texas/models > /local/s/datasets/texas/results.txt

java ssd.TrainModel /local/s/datasets/wisconsin/train /local/s/datasets/wisconsin/test /local/s/datasets/wisconsin/test-results /local/s/datasets/wisconsin/models
java ssd.TestModel /local/s/wisconsin/datasets/test /local/s/wisconsin/datasets/test-results /local/s/datasets/wisconsin/models > /local/s/datasets/wisconsin/results.txt

java ssd.TrainModel /local/s/datasets/20news/train /local/s/datasets/washington/test /local/s/datasets/washington/test-results /local/s/datasets/washington/models
java ssd.TestModel /local/s/washington/datasets/test /local/s/washington/datasets/test-results /local/s/datasets/washington/models > /local/s/datasets/washington/results.txt

java ssd.TrainModel /local/s/datasets/misc/train /local/s/datasets/misc/test /local/s/datasets/misc/test-results /local/s/datasets/misc/models
java ssd.TestModel /local/s/misc/datasets/test /local/s/misc/datasets/test-results /local/s/datasets/misc/models > /local/s/datasets/misc/results.txt