#!/usr/bin/perl -w
    use lib './perl-includes';
    use HTML::Scrubber;
    use strict;
                                                                            #
    my $html = q[
    <style type="text/css"> BAD { background: #666; color: #666;} </style>
    <script language="javascript"> alert("Hello, I am EVIL!");    </script>
    <HR>
        a   => <a href=1>link </a>
        br  => <br>
        b   => <B> bold </B>
        u   => <U> UNDERLINE </U>
    ];
                                                                            #
    my $scrubber = HTML::Scrubber->new( allow => [ qw[ p b i u hr br ] ] ); #
                                                                            #
    print $scrubber->scrub($html);                                          #
                                                                            #
    $scrubber->deny( qw[ p b i u hr br ] );                                 #
                                                                            #
    print $scrubber->scrub($html);                                          #
                                                                            #
