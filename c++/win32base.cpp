#include "stdafx.h"
#include "win32base.h"

CThread::CThread() 
	:m_hThread(NULL)
{
	m_bRunning = false;
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
	m_hThread = CreateThread(NULL, 0, CThread::_ThreadFunction, ptr, 0, &m_dwThreadID);
	if (m_hThread == INVALID_HANDLE_VALUE)
	{
		m_bRunning = false;
		return FALSE;
	}
	return TRUE;
}

void CThread::Join()
{
	if (m_hThread && m_hThread != INVALID_HANDLE_VALUE) 
	{
		WaitForSingleObject(m_hThread, INFINITE);
		m_hThread = INVALID_HANDLE_VALUE;
	}
}