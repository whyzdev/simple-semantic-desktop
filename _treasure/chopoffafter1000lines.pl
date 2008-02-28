#!/usr/bin/perl
@list = 'ls *.txt';

foreach(@list){
    exec "head -n 3000 $1 > $1"

}
