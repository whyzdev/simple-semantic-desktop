#!/usr/bin/perl
for ($count = 1000; $count <=9000; $count++){
    print "wget -nd -r -l 1 -A txt -R zip,rtf,html http://www.gutenberg.org/etext/$count\n";
}
