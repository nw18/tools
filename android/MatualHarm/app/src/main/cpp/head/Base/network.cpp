/*
 * Network.cpp
 *
 *  Created on: Apr 5, 2017
 *      Author: newind
 */
#include <stdarg.h>
#include "network.h"

void Network::BlockCommand::print(const char *format,...){
	va_list arg_list;
	va_start(arg_list,format);
	content_size += snprintf(data + content_size,sizeof(data) - content_size - 1,format,arg_list);
	va_end(arg_list);
}
