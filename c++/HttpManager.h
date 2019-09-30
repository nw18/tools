#pragma once
#include <map>
#include <string>

class HttpHead
{
public:
	typedef std::map<std::string, std::string> _HeadPropsMap;

	const std::string GetField(const std::string &key);
	void SetField(const std::string &key, const std::string &value);
	void SetField(const std::string &key, const char *fmt, ...);

private:
	_HeadPropsMap m_mapFields;
};

class HttpBody
{
public:
	
};

class HttpRequest
{
public:

};

class HttpResponse
{
public:

};

class HttpDemo
{
public:
};

class HttpManager
{
public:
	HttpManager();
	static HttpManager *instance(void);
	//·±Ã¦³¬Ê±Ê±¼ä
	void SetBlockTimeout(DWORD dwTimeout);
	DWORD GetBlockTimeout(void);

	int AddRequest(HttpRequest *pRequest, int nTimeout = -1);

protected:

	virtual ~HttpManager();
};

class HttpBuilder
{

};

