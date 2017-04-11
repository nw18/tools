/*
 * task.cpp
 *
 *  Created on: 2017年4月5日
 *      Author: newind
 */

#include "task.h"

void *ITask::thread_hook(void *ptr){
	static_cast<ITask*>(ptr)->run();
	return ptr;
}

