#!/usr/bin/perl -w
   use File::Basename;
@files = <$ARGV[0]*>;
foreach $file (@files) {  
#  print $file . "\n";
  system "./html2txt < " . $file . " > " . $ARGV[1] . "/".fileparse($file) . "\n";
}
