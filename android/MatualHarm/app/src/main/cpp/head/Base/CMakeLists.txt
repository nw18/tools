set(BASE_SRCS
    task.cpp
    network.cpp)

set(CMAKE_EXE_LINKER_FLAGS "-lpthread -lrt")

add_library(Base STATIC
    ${BASE_SRCS})