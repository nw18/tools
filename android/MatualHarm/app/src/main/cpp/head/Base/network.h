/*
 * Network.h
 *
 *  Created on: Apr 5, 2017
 *      Author: newind
 */

#ifndef NETWORK_H_
#define NETWORK_H_

#include "base.h"
#define MAX_BLOCK_SIZE (4*1024)

class Network {
public:
	enum BlockType{
		BT_NULL = 0,
		BT_COMMAND,
		BT_BINARY,
	};

	struct BlockHead{
		unsigned char block_type;
		unsigned short content_size;
		unsigned short sumSize() {
			 return content_size + sizeof(BlockHead);
		}
	};

	struct BlockBuffer : BlockHead{
		BlockBuffer(){
			memset(this,0,sizeof(*this));
		}
		char data[MAX_BLOCK_SIZE - sizeof(BlockHead)];
	};

	struct BlockNull : BlockHead {
		BlockNull(){
			block_type = BT_NULL;
			strncpy(data,"BlockNull",sizeof(data));
			content_size = strlen(data);
		}
		char data[16];
	};

	struct BlockCommand : BlockBuffer {
		BlockCommand(){
			block_type = BT_COMMAND;
			content_size = 0;
		}

		void print(const char *format,...);
	};

	template<class Class>
	struct BlockBinary : BlockHead{
		BlockBinary(){
			block_type = BT_BINARY;
			content_size = sizeof(Class);
			memset(name,0,sizeof(name));
			memset(&object,0,sizeof(object));
			strncpy(name,typeid(Class).name(),sizeof(name));
		}
		char name[128];
		Class object;
	};
};

#endif /* NETWORK_H_ */
