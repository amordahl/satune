# Requirements

## Using the Provided VM

We provide a VM, so all that is required is VirtualBox and at least 12 GB of storage. VirtualBox is available for all major environments (see: https://www.virtualbox.org/wiki/Downloads). 

The details of the VM settings can be seen in `SATune-VM-settings.png`.

This is the shortest path which should not require any interaction with the developers of SATune.

## Intalling SATune and the subject verification tools

- **OS**: Ubuntu 16.04
- **Python**: 3.7+
- **Java**: 1.8+
- **LLVM** 4.0.1

And, install all the dependencies for the verification tools following the instructions in the `experiments/tools/<tool>/InstallationNotes.md` files (mainly, the SMT solvers).

This path is significantly longer and complicated which is likely to require communicating with the developers of SATune.