package me.tatarka.bindingcollectionadapter2.collections;

import android.databinding.ListChangeRegistry;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An {@link ObservableList} that presents multiple lists and items as one contiguous source.
 * Changes to any of the given lists will be reflected here. You cannot modify {@code
 * MergeObservableList} itself other than adding and removing backing lists or items with {@link
 * #insertItem(Object)} and {@link #insertList(ObservableList)} respectively.  This is a good case
 * where you have multiple data sources, or a handful of fixed items mixed in with lists of data.
 */
public class MergeObservableList<T> extends AbstractList<T> implements ObservableList<T> {

    final ArrayList<List<? extends T>> lists = new ArrayList<>();
    final ListChangeCallback callback = new ListChangeCallback();
    final ListChangeRegistry listeners = new ListChangeRegistry();

    Source<T> source;

    @Override
    public void addOnListChangedCallback(OnListChangedCallback<? extends ObservableList<T>> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeOnListChangedCallback(OnListChangedCallback<? extends ObservableList<T>> listener) {
        listeners.remove(listener);
    }

    /**
     * Inserts the given item into the merge list.
     *
     * @deprecated Use {@link Source#insertItem(Object)} instead.
     */
    @Deprecated
    public MergeObservableList<T> insertItem(T object) {
        source().insertItem(object);
        return this;
    }

    /**
     * Inserts the given {@link ObservableList} into the merge list. Any changes in the given list
     * will be reflected and propagated here.
     *
     * @deprecated Use {@link Source#insertList(ObservableList)} instead.
     */
    @Deprecated
    public MergeObservableList<T> insertList(@NonNull ObservableList<? extends T> list) {
        source().insertList(list);
        return this;
    }

    /**
     * Removes the given item from the merge list.
     *
     * @deprecated Use {@link Source#removeItem(Object)} instead.
     */
    @Deprecated
    public boolean removeItem(T object) {
        source().removeItem(object);
        return false;
    }

    /**
     * Removes the given {@link ObservableList} from the merge list.
     *
     * @deprecated Use {@link Source#removeList(ObservableList)} instead.
     */
    @Deprecated
    public boolean removeList(ObservableList<? extends T> listToRemove) {
        source().removeList(listToRemove);
        return false;
    }

    /**
     * Removes all items and lists from the merge list.
     *
     * @deprecated Use {@link Source#removeAll()} instead.
     */
    @Deprecated
    public void removeAll() {
        source().removeAll();
    }

    /**
     * Converts an index into this merge list into an into an index of the given backing list.
     *
     * @throws IndexOutOfBoundsException for an invalid index.
     * @throws IllegalArgumentException  if the given list is not backing this merge list.
     */
    public int mergeToBackingIndex(ObservableList<? extends T> backingList, int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        int size = 0;
        for (int i = 0, listsSize = lists.size(); i < listsSize; i++) {
            List<? extends T> list = lists.get(i);
            if (backingList == list) {
                if (index < list.size()) {
                    return size + index;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }
            size += list.size();
        }
        throw new IllegalArgumentException();
    }

    /**
     * Converts an index into a backing list into an index into this merge list.
     *
     * @throws IndexOutOfBoundsException for an invalid index.
     * @throws IllegalArgumentException  if the given list is not backing this merge list.
     */
    public int backingIndexToMerge(ObservableList<? extends T> backingList, int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        int size = 0;
        for (int i = 0, listsSize = lists.size(); i < listsSize; i++) {
            List<? extends T> list = lists.get(i);
            if (backingList == list) {
                if (index - size < list.size()) {
                    return index - size;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }
            size += list.size();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public T get(int location) {
        if (location < 0) {
            throw new IndexOutOfBoundsException();
        }
        int size = 0;
        for (int i = 0, listsSize = lists.size(); i < listsSize; i++) {
            List<? extends T> list = lists.get(i);
            if (location - size < list.size()) {
                return list.get(location - size);
            }
            size += list.size();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        int size = 0;
        for (int i = 0, listsSize = lists.size(); i < listsSize; i++) {
            List<? extends T> list = lists.get(i);
            size += list.size();
        }
        return size;
    }

    private Source<T> source() {
        if (source == null) {
            new Source<>(this);
        }
        return source;
    }

    class ListChangeCallback extends OnListChangedCallback {

        @Override
        public void onChanged(ObservableList sender) {
            modCount += 1;
            listeners.notifyChanged(MergeObservableList.this);
        }

        @Override
        public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
            int size = 0;
            for (int i = 0, listsSize = lists.size(); i < listsSize; i++) {
                List list = lists.get(i);
                if (list == sender) {
                    listeners.notifyChanged(MergeObservableList.this, size + positionStart, itemCount);
                    return;
                }
                size += list.size();
            }
        }

        @Override
        public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
            modCount += 1;
            int size = 0;
            for (int i = 0, listsSize = lists.size(); i < listsSize; i++) {
                List list = lists.get(i);
                if (list == sender) {
                    listeners.notifyInserted(MergeObservableList.this, size + positionStart, itemCount);
                    return;
                }
                size += list.size();
            }
        }

        @Override
        public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
            int size = 0;
            for (int i = 0, listsSize = lists.size(); i < listsSize; i++) {
                List list = lists.get(i);
                if (list == sender) {
                    listeners.notifyMoved(MergeObservableList.this, size + fromPosition, size + toPosition, itemCount);
                    return;
                }
                size += list.size();
            }
        }

        @Override
        public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
            modCount += 1;
            int size = 0;
            for (int i = 0, listsSize = lists.size(); i < listsSize; i++) {
                List list = lists.get(i);
                if (list == sender) {
                    listeners.notifyRemoved(MergeObservableList.this, size + positionStart, itemCount);
                    return;
                }
                size += list.size();
            }
        }
    }

    public static class Source<T> {
        private MergeObservableList<T> mergeList;

        public Source() {
            this(new MergeObservableList<T>());
        }

        Source(MergeObservableList<T> list) {
            mergeList = list;
            mergeList.source = this;
        }

        /**
         * Inserts the given item into the merge list.
         */
        public Source<T> insertItem(T object) {
            mergeList.lists.add(Collections.singletonList(object));
            mergeList.modCount += 1;
            mergeList.listeners.notifyInserted(mergeList, mergeList.size() - 1, 1);
            return this;
        }

        /**
         * Inserts the given {@link ObservableList} into the merge list. Any changes in the given list
         * will be reflected and propagated here.
         */
        @SuppressWarnings("unchecked")
        public Source<T> insertList(@NonNull ObservableList<? extends T> list) {
            list.addOnListChangedCallback(mergeList.callback);
            int oldSize = mergeList.size();
            mergeList.lists.add(list);
            mergeList.modCount += 1;
            if (!list.isEmpty()) {
                mergeList.listeners.notifyInserted(mergeList, oldSize, list.size());
            }
            return this;
        }

        /**
         * Removes the given item from the merge list.
         */
        public boolean removeItem(T object) {
            int size = 0;
            for (int i = 0, listsSize = mergeList.lists.size(); i < listsSize; i++) {
                List<? extends T> list = mergeList.lists.get(i);
                if (!(list instanceof ObservableList)) {
                    Object item = list.get(0);
                    if ((object == null) ? (item == null) : object.equals(item)) {
                        mergeList.lists.remove(i);
                        mergeList.modCount += 1;
                        mergeList.listeners.notifyRemoved(mergeList, size, 1);
                        return true;
                    }
                }
                size += list.size();
            }
            return false;
        }

        /**
         * Removes the given {@link ObservableList} from the merge list.
         */
        @SuppressWarnings("unchecked")
        public boolean removeList(ObservableList<? extends T> listToRemove) {
            int size = 0;
            for (int i = 0, listsSize = mergeList.lists.size(); i < listsSize; i++) {
                List<? extends T> list = mergeList.lists.get(i);
                if (list == listToRemove) {
                    listToRemove.removeOnListChangedCallback(mergeList.callback);
                    mergeList.lists.remove(i);
                    mergeList.modCount += 1;
                    mergeList.listeners.notifyRemoved(mergeList, size, list.size());
                    return true;
                }
                size += list.size();
            }
            return false;
        }

        /**
         * Removes all items and lists from the merge list.
         */
        public void removeAll() {
            int size = mergeList.size();
            for (int i = 0, listSize = mergeList.lists.size(); i < listSize; i++) {
                List<? extends T> list = mergeList.lists.get(i);
                if (list instanceof ObservableList) {
                    ((ObservableList) list).removeOnListChangedCallback(mergeList.callback);
                }
            }
            mergeList.lists.clear();
            mergeList.modCount += 1;
            mergeList.listeners.notifyRemoved(mergeList, 0, size);
        }

        public ObservableList<T> list() {
            return mergeList;
        }
    }
}
