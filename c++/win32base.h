#pragma once
#include <vector>
#include <memory>
#include <functional>
#include <list>

template<class T>
class SharedPtr : public std::shared_ptr < T > {
public:
	SharedPtr() { }
	SharedPtr(T* ptr) {
		this->reset(ptr);
	}
};

class CWin32Lock{
private:
	CRITICAL_SECTION m_sec;
public:
	CWin32Lock() {
		InitializeCriticalSection(&m_sec);
	}
	~CWin32Lock() {
		DeleteCriticalSection(&m_sec);
	}
	void Lock() {
		EnterCriticalSection(&m_sec);
	}
	void Unlock() {
		LeaveCriticalSection(&m_sec);
	}
};

class CThread {
public:
	typedef std::shared_ptr<CThread> Ptr;
	static Ptr safeSetup(CThread *pThread);

	CThread();
	CThread(int nNum);
	virtual ~CThread();

	void Join();
	bool IsRunning() { return m_bRunning; }
	BOOL SetupThread();
protected:
	BOOL _SetupThread(void *ptr);
	virtual void Run(void) = 0;

	HANDLE m_hThread[10];
	DWORD m_dwThreadID[10];
	int m_nThreadNum;
private:
	bool m_bRunning;
	static DWORD __stdcall _ThreadFunction(LPVOID pUser);
};

template<class T>
std::shared_ptr<T> SafeSetup(T* t)
{
	CThread::Ptr ptr = CThread::safeSetup(t);
	return *(std::shared_ptr<T>*)&ptr;
}

template<class T, class _Ptr = std::shared_ptr<T>>
class MessageQueue
{
public:
	typedef std::list<_Ptr> _List;

	MessageQueue(int nMax = 1024 * 1024)
	{
		m_hRead = CreateSemaphore(NULL, 0, nMax, NULL);
		m_hWrite = CreateSemaphore(NULL, nMax, nMax, NULL);
	}

	virtual ~MessageQueue()
	{
		CloseHandle(m_hRead);
		CloseHandle(m_hWrite);
	}

	const _Ptr Shift(DWORD dwTimeout = INFINITE, BOOL *bTimetout = NULL)
	{
		_Ptr ptr;
		DWORD dwRes = WaitForSingleObject(m_hRead, dwTimeout);
		if (dwRes == WAIT_OBJECT_0)
		{
			m_lock.Lock();
			ptr = m_listMessage.front();
			m_listMessage.pop_front();
			m_lock.Unlock();
			ReleaseSemaphore(m_hWrite, 1, NULL);
		}
		else if (dwRes == WAIT_TIMEOUT && bTimetout)
		{
			*bTimetout = TRUE;
		}

		return ptr;
	}
	void Push(const _Ptr &ptr, DWORD dwTimeout = INFINITE)
	{
		WaitForSingleObject(m_hWrite, dwTimeout);
		m_lock.Lock();
		m_listMessage.push_back(ptr);
		m_lock.Unlock();
		ReleaseSemaphore(m_hRead, 1, NULL);
	}
private:
	_List m_listMessage;
	HANDLE m_hRead;
	HANDLE m_hWrite;
	CWin32Lock m_lock;
};

template<class T, class _Ptr = std::shared_ptr<T>>
class CMessageThread : public CThread
{
public:
	CMessageThread(int nThreadNum = 1, int nQMax = 1024 ,DWORD dwTimeout = INFINITE)
		:CThread(nThreadNum),
		m_queue(nQMax)
	{
		m_bQuit = false;
		m_dwTimeout = dwTimeout;
	}

	typedef MessageQueue<T, _Ptr> _MessageQueue;

	_MessageQueue& messageQueue() { return m_queue; }
	void SetQuit(bool bQuit) { m_bQuit = true; }
protected:
	_MessageQueue m_queue;
	virtual void OnIdle(void) { };
	virtual void HandleMessage(const _Ptr& ptr) = 0;
	virtual void Run(void) 
	{
		while (!m_bQuit)
		{
			BOOL bTimeout = FALSE;
			_Ptr ptr = m_queue.Shift(m_dwTimeout, &bTimeout);
			if (bTimeout) 
			{
				this->OnIdle();
			}
			else
			{
				this->HandleMessage(ptr);
			}
		}
	}

	bool m_bQuit;
	DWORD m_dwTimeout;
};
