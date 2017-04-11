//
// Created by Administrator on 2017/3/24.
//

#ifndef MUTUALHARM_HARM_H_H
#define MUTUALHARM_HARM_H_H

#include "Box2D/Box2D.h"
#include "Base/task.h"

class HarmWorld {
private:
    HarmWorld();
    b2World mWorld;
    ITask *p_task_step_in;
    void run();
public:
    ~HarmWorld();
    int setup();
    void release();
};

#endif //MUTUALHARM_HARM_H_H
