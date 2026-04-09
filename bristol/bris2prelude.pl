use strict;
use List::MoreUtils qw(uniq);


my $AND = "AND";
my $INV = "INV";
my $XOR = "XOR";
my @output_list = qw();
my @input_list_l = qw();
my @input_list_r = qw();
my $num_values = 64;
my $counter = 0;

open my $logfile, '64bit-adder.txt'; 
while (<$logfile>)
{
    chomp;
    my $gate = $_;

    my ($gt) = ($gate =~ /(\w+)$/);

    if ($gt eq $XOR)
    {
    my ($inwl, $inwr, $outw) = (
        $gate =~ /\d \d (\d+) (\d+) (\d+)/
    );
	print "xorgmw(\"$outw\", \"$inwl\", \"$inwr\");\n"; 
    push(@output_list, $outw);
    push(@input_list_l, $inwl);
    push(@input_list_r, $inwr);
    }

    if ($gt eq $INV)
    {
	my ($inw, $outw) = ($gate =~ /\d \d (\d+) (\d+)/);
	print "notgmw(\"$outw\", \"$inw\");\n";
    }

    if ($gt eq $AND)
    {
	my ($inwl, $inwr, $outw) = ($gate =~ /\d \d (\d+) (\d+) (\d+)/);
	print "andgmw(\"$outw\", \"$inwl\", \"$inwr\");\n";
    push(@output_list, $outw);
    push(@input_list_l, $inwl);
    push(@input_list_r, $inwr);
    }
}
# INPUTS
# Sort inputs so they are in order, make them unique, then reverse order
# Get first 128 (64 per wire)
@input_list_l = sort {$a <=> $b} uniq @input_list_l;
@input_list_r = sort {$a <=> $b} uniq @input_list_r;
my @first_inputs_l = reverse(@input_list_l[0..$num_values - 1]);
my @first_inputs_r = reverse(@input_list_r[0..$num_values - 1]);

# OUTPUTS
# Get 64 highest
@output_list = sort {$a <=> $b} uniq @output_list;
my $o_list_size = $#output_list;
my @last_outputs = reverse(
    @output_list[$o_list_size - $num_values + 1..$o_list_size]
);

# Postcondition 
print "postcondition: (|";
$counter = 0;
foreach (@last_outputs) {
    print"RECON(\"$_\")";
    if ($counter < $num_values - 1) {
        print ", ";
    }
    $counter = $counter + 1;
}
print "| == ";
$counter = 0;
print "BVAdd(|";
foreach (@first_inputs_l) {
    print"RECON(\"$_\")";
    if ($counter < $num_values - 1) {
        print ", ";
    }
    $counter = $counter + 1;
}
print "|, |";
$counter = 0;
foreach (@first_inputs_r) {
    print"RECON(\"$_\")";
    if ($counter < $num_values - 1) {
        print ", ";
    }
    $counter = $counter + 1;

}
print "|))\n";
