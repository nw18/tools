/*
 * task.h
 *
 *  Created on: 2017年4月5日
 *      Author: newind
 */

#ifndef SRC_BASE_TASK_H_
#define SRC_BASE_TASK_H_

#include "base.h"

class ITask {
public:
	static void *thread_hook(void *);
	virtual ~ITask() { };
	virtual int join() = 0;
protected:
	virtual void run() = 0;
};

template<class C>
class Task : protected ITask{
protected:
	void run(){
		(cPtr->*funPtr)();
	}
public:
	typedef void (C::*FunPtr)();
	Task(C *cPtr,FunPtr funPtr)
	:cPtr(cPtr),funPtr(funPtr) ,threadID(0){ }
	virtual ~Task() { }
	int start(bool autoDetach = false){
		if(pthread_create(&threadID,NULL,&ITask::thread_hook,this) != 0){
			return -1;
		}
		if(autoDetach){
			pthread_detach(threadID);
		}
		return 0;
	}

	int join(){
		return pthread_join(threadID,NULL);
	}

	C* host() { return cPtr; }

	static ITask *create(C *cPtr,FunPtr funPtr){
		Task *task = new Task<C>(cPtr,funPtr);
		if(0 != task->start(false)){
			return NULL;
		}
		return task;
	}
private:
	C *cPtr;
	FunPtr funPtr;
	pthread_t threadID;
};

#endif /* SRC_BASE_TASK_H_ */
