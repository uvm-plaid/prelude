use strict;

my $AND = "AND";
my $INV = "INV";

while (<>)
{
    chomp;
    my $gate = $_;

    my ($gt) = ($gate =~ /(\w+)$/);

    if ($gt eq $INV)
    {
	my ($inw, $outw) = ($gate =~ /\d \d (\d+) (\d+)/);
	print "notgmw(\"$outw\", \"$inw\");\n";
    }

    if ($gt eq $AND)
    {
	my ($inwl, $inwr, $outw) = ($gate =~ /\d \d (\d+) (\d+) (\d+)/);
	print "andgmw(\"$outw\", \"$inwl\", \"$inwr\");\n";
    }
}
