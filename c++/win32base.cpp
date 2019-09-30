#include "stdafx.h"
#include "win32base.h"

CThread::CThread() 
	:m_nThreadNum(1)
{
	m_bRunning = false;
	memset(m_hThread, 0, sizeof(m_hThread));
	memset(m_dwThreadID, 0, sizeof(m_dwThreadID));
}

CThread::CThread(int nNum)
	: m_nThreadNum(nNum)
{
	assert(nNum > 0 && nNum <= sizeof(m_hThread)/sizeof(m_hThread[0]));
	m_bRunning = false;
	memset(m_hThread, 0, sizeof(m_hThread));
	memset(m_dwThreadID, 0, sizeof(m_dwThreadID));
}

CThread::~CThread() 
{

}

//////////////////////////////////////////////////////////////////////////
const int nSlotCount = 1024;
CWin32Lock sSlotLock;
CThread::Ptr sSlotArray[nSlotCount];

CThread::Ptr CThread::safeSetup(CThread *pThread)
{
	CThread::Ptr ptr(pThread);
	sSlotLock.Lock();
	for (int i = 0; i < nSlotCount; i++)
	{
		if (sSlotArray[i].get() == nullptr) 
		{
			sSlotArray[i] = ptr;
			sSlotLock.Unlock();
			return ptr;
		}
	}
	sSlotLock.Unlock();

	ptr.reset();
	return ptr;
}

DWORD __stdcall CThread::_ThreadFunction(LPVOID pUser)
{
	if (pUser >= &sSlotArray[0] && pUser <= &sSlotArray[nSlotCount - 1])
	{
		CThread::Ptr ptr = *((CThread::Ptr*)pUser);
		ptr->Run();
		ptr->m_bRunning = false;
		sSlotLock.Lock();
		((CThread::Ptr*)pUser)->reset();
		sSlotLock.Unlock();
	}
	else
	{
		((CThread*)pUser)->Run();
	}
	return 0;
}

BOOL CThread::SetupThread()
{
	return _SetupThread(this);
}

BOOL CThread::_SetupThread(void *ptr)
{
	m_bRunning = true;
	for (int i = 0; i < m_nThreadNum; i++)
	{
		m_hThread[i] = CreateThread(NULL, 0, CThread::_ThreadFunction, ptr, 0, &m_dwThreadID[i]);
		if (m_hThread == INVALID_HANDLE_VALUE)
		{
			m_bRunning = false;
			return FALSE;
		}
	}
	return TRUE;
}

void CThread::Join()
{
	if (m_hThread[0] && m_hThread[0] != INVALID_HANDLE_VALUE) 
	{
		WaitForMultipleObjects(m_nThreadNum, m_hThread, TRUE, INFINITE);
		memset(m_hThread, 0, sizeof(m_hThread));
	}
}