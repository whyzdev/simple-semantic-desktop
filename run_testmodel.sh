#!/bin/sh

java ssd.TestModel /local/s/datasets/cornell/test /local/s/datasets/cornell/test-results /local/s/datasets/cornell/models > /local/s/datasets/washington/cornresults.txt

java ssd.TestModel /local/s/datasets/texas/test /local/s/datasets/texas/test-results /local/s/datasets/texas/models > /local/s/datasets/washington/texresults.txt

java ssd.TestModel /local/s/datasets/wisconsin/test /local/s/datasets/wisconsin/test-results /local/s/datasets/wisconsin/models > /local/s/datasets/washington/wiscresults.txt

java ssd.TestModel /local/s/datasets/washington/test /local/s/datasets/washington/test-results /local/s/datasets/washington/models > /local/s/datasets/washington/wasresults.txt

java ssd.TestModel /local/s/datasets/misc/test /local/s/datasets/misc/test-results /local/s/datasets/misc/models > /local/s/datasets/washington/misresults.txt
