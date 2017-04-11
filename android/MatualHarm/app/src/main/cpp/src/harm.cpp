//
// Created by Administrator on 2017/3/24.
//
#include <string>
#include "harm.h"

HarmWorld::HarmWorld()
        : mWorld(b2Vec2(0, 0))
        , p_task_step_in(NULL){
}

HarmWorld::~HarmWorld() {

}

void HarmWorld::run() {

}

int HarmWorld::setup() {
    p_task_step_in = Task<HarmWorld>::create(this,&HarmWorld::run);
    if (NULL == p_task_step_in){
        return -1;
    }
    return 0;
}

void HarmWorld::release() {
    if (NULL != p_task_step_in){
        p_task_step_in->join();
        delete p_task_step_in;
        p_task_step_in = NULL;
    }
}

