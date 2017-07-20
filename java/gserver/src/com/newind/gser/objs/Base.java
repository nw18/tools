package com.newind.gser.objs;

import java.util.*;

public class Base {
    private String ID;
    private long createTime;
    private long updateTime;

    Base() {
        ID = UUID.randomUUID().toString();
        updateTime = createTime = System.currentTimeMillis();
    }

    public String getID() {
        return ID;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isTimeout() {
        return false;
    }

    public boolean canTimeout() {
        return false;
    }

    public static class ManagerBase<T extends Base> {
        protected HashMap<String, T> objectMap = new HashMap<>();

        public void add(T object) {
            synchronized (objectMap) {
                if(objectMap.containsKey(object.getID())){
                    T origin = objectMap.get(object.getID());
                    if (origin.equals(object)){
                        return;
                    }
                    if (origin.canTimeout()) {
                        onDelHook(origin);
                    }
                }
                objectMap.put(object.getID(), object);
                if(object.canTimeout()) {
                    onAddHook(object);
                }
            }
        }

        public void delete(String id) {
            T object = null;
            synchronized (objectMap) {
                object = objectMap.remove(id);
                if (object.canTimeout()) {
                    onDelHook(object);
                }
            }
        }

        public T find(String id) {
            synchronized (objectMap) {
                return objectMap.get(id);
            }
        }

        protected void onAddHook(T object){
            TimeoutManager.getInstance().add(object);
        }

        protected void onDelHook(T object) {
            TimeoutManager.getInstance().delete(object.getID());
        }
    }

    public static class TimeoutManager extends ManagerBase<Base>{
        private TimeoutManager() {

        }

        @Override
        protected void onAddHook(Base object) {
            //TODO event may be needed
        }

        @Override
        protected void onDelHook(Base object) {
            //TODO event may be needed
        }

        private static TimeoutManager _inst_ = new TimeoutManager();
        public static TimeoutManager getInstance() {
            return _inst_;
        }

        public List<Base> getAllTimeout(boolean doRemove) {
            LinkedList<Base> list;
            synchronized (objectMap) {
                list = new LinkedList<>(objectMap.values());

                Iterator<Base> it = list.iterator();
                while (it.hasNext()){
                    Base object = it.next();
                    if (object.isTimeout()) {
                        it.remove();

                    }
                }
            }
            return list;
        }
    }
}
