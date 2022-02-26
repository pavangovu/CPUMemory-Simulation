# CPUMemory-Simulation
The purpose of this application is to use Java to simulate a simple computer system consisting of a CPU and Memory. There is a process emulating main memory and another process emulating the CPU. These two processes communicate via pipes. The program also implements a fetch-execute instruction cycle, in which the CPU sends the address over to memory over pipes, and the memory retrieves the instruction from said address and pipes it back to the CPU for execution. The system also incorporates interrupt processing. The instruction cycle can be interrupted by either a timer or a system call, incorporating the use of a handler.

## CPU
Supports functions like fetching instructions from Memory, executing instructions, pushing values to Memory stack and poping values from Memory stack.

## Memory
Supports functions like loading instructions from a file, passing instructions to Cpu and updating values on the stack.

