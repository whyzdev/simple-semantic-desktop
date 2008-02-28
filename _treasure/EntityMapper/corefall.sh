#!/bin/bash
cat $1/temp_entity/* | ./cmd_coref_en_news_muc6.sh -resultType=conf > $1/_entityanalysis
