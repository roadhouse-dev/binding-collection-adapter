package me.tatarka.bindingcollectionadapter2;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import me.tatarka.bindingcollectionadapter2.collections.MergeObservableList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(JUnit4.class)
@SuppressWarnings("unchecked")
public class MergeObservableTest {
    @Test
    public void emptyListIsEmpty() {
        MergeObservableList<String> list = new MergeObservableList<>();
        ObservableList.OnListChangedCallback callback = mock(ObservableList.OnListChangedCallback.class);
        list.addOnListChangedCallback(callback);

        assertThat(list).isEmpty();
        verifyNoMoreInteractions(callback);
    }

    @Test
    public void insertingItemContainsThatItem() {
        MergeObservableList.Source<String> source = new MergeObservableList.Source<>();
        ObservableList<String> list = source.list();
        ObservableList.OnListChangedCallback callback = mock(ObservableList.OnListChangedCallback.class);
        list.addOnListChangedCallback(callback);
        source.insertItem("test");

        assertThat(list)
                .hasSize(1)
                .containsExactly("test");
        verify(callback).onItemRangeInserted(list, 0, 1);
    }

    @Test
    public void insertingListContainsThatList() {
        MergeObservableList.Source<String> source = new MergeObservableList.Source<>();
        ObservableList<String> list = source.list();
        ObservableList.OnListChangedCallback callback = mock(ObservableList.OnListChangedCallback.class);
        list.addOnListChangedCallback(callback);
        ObservableList<String> items = new ObservableArrayList<>();
        items.add("test1");
        items.add("test2");
        source.insertList(items);

        assertThat(list)
                .hasSize(2)
                .containsExactly("test1", "test2");
        verify(callback).onItemRangeInserted(list, 0, 2);
    }

    @Test
    public void insertingItemAndListContainsItemThenList() {
        MergeObservableList.Source<String> source = new MergeObservableList.Source<>();
        ObservableList<String> list = source.list();
        ObservableList.OnListChangedCallback callback = mock(ObservableList.OnListChangedCallback.class);
        list.addOnListChangedCallback(callback);
        source.insertItem("test1");
        ObservableList<String> items = new ObservableArrayList<>();
        items.add("test2");
        items.add("test3");
        source.insertList(items);

        assertThat(list)
                .hasSize(3)
                .containsExactly("test1", "test2", "test3");
        verify(callback).onItemRangeInserted(list, 0, 1);
        verify(callback).onItemRangeInserted(list, 1, 2);
    }

    @Test
    public void addingItemToBackingListAddsItemToList() {
        MergeObservableList.Source<String> source = new MergeObservableList.Source<>();
        ObservableList<String> list = source.list();
        ObservableList.OnListChangedCallback callback = mock(ObservableList.OnListChangedCallback.class);
        list.addOnListChangedCallback(callback);
        source.insertItem("test1");
        ObservableList<String> items = new ObservableArrayList<>();
        items.add("test2");
        source.insertList(items);
        source.insertItem("test4");
        items.add("test3");

        assertThat(list)
                .hasSize(4)
                .containsExactly("test1", "test2", "test3", "test4");
        verify(callback).onItemRangeInserted(list, 0, 1);
        verify(callback).onItemRangeInserted(list, 1, 1);
        verify(callback, times(2)).onItemRangeInserted(list, 2, 1);
    }

    @Test
    public void removingItemFromBackingListRemovesItemFromList() {
        MergeObservableList.Source<String> source = new MergeObservableList.Source<>();
        ObservableList<String> list = source.list();
        ObservableList.OnListChangedCallback callback = mock(ObservableList.OnListChangedCallback.class);
        list.addOnListChangedCallback(callback);
        source.insertItem("test1");
        ObservableList<String> items = new ObservableArrayList<>();
        items.add("test2");
        source.insertList(items);
        source.insertItem("test3");
        items.clear();

        assertThat(list)
                .hasSize(2)
                .containsExactly("test1", "test3");
        verify(callback).onItemRangeInserted(list, 0, 1);
        verify(callback).onItemRangeInserted(list, 1, 1);
        verify(callback).onItemRangeInserted(list, 2, 1);
        verify(callback).onItemRangeRemoved(list, 1, 1);
    }
    
    @Test
    public void changingItemFromBackingListChangesItInList() {
        MergeObservableList.Source<String> source = new MergeObservableList.Source<>();
        ObservableList<String> list = source.list();
        ObservableList.OnListChangedCallback callback = mock(ObservableList.OnListChangedCallback.class);
        list.addOnListChangedCallback(callback);
        source.insertItem("test1");
        ObservableList<String> items = new ObservableArrayList<>();
        items.add("test2");
        source.insertList(items);
        items.set(0, "test3");
        
        assertThat(list)
                .hasSize(2)
                .containsExactly("test1", "test3");
        verify(callback).onItemRangeInserted(list, 0, 1);
        verify(callback).onItemRangeInserted(list, 1, 1);
        verify(callback).onItemRangeChanged(list, 1, 1);
    }

    @Test
    public void removingItemRemovesItFromTheList() {
        MergeObservableList.Source<String> source = new MergeObservableList.Source<>();
        ObservableList<String> list = source.list();
        source.insertItem("test1");
        ObservableList.OnListChangedCallback callback = mock(ObservableList.OnListChangedCallback.class);
        list.addOnListChangedCallback(callback);
        source.removeItem("test1");

        assertThat(list).isEmpty();
        verify(callback).onItemRangeRemoved(list, 0, 1);
    }

    @Test
    public void removingListRemovesItFromTheList() {
        MergeObservableList.Source<String> source = new MergeObservableList.Source<>();
        ObservableList<String> list = source.list();
        ObservableList<String> backingList = new ObservableArrayList<>();
        backingList.addAll(Arrays.asList("test1", "test2"));
        source.insertList(backingList);
        ObservableList.OnListChangedCallback callback = mock(ObservableList.OnListChangedCallback.class);
        list.addOnListChangedCallback(callback);
        source.removeList(backingList);

        assertThat(list).isEmpty();
        verify(callback).onItemRangeRemoved(list, 0, 2);
    }

    @Test
    public void removingAllRemovesInsertedItemFromTheList() {
        MergeObservableList.Source<String> source = new MergeObservableList.Source<>();
        ObservableList<String> list = source.list();
        source.insertItem("test1");
        ObservableList.OnListChangedCallback callback = mock(ObservableList.OnListChangedCallback.class);
        list.addOnListChangedCallback(callback);
        source.removeAll();

        assertThat(list).isEmpty();
        verify(callback).onItemRangeRemoved(list, 0, 1);
    }

    @Test
    public void removingAllRemovesInsertedListFromTheList() {
        MergeObservableList.Source<String> source = new MergeObservableList.Source<>();
        ObservableList<String> list = source.list();
        ObservableList<String> backingList = new ObservableArrayList<>();
        backingList.addAll(Arrays.asList("test1", "test2"));
        source.insertList(backingList);
        ObservableList.OnListChangedCallback callback = mock(ObservableList.OnListChangedCallback.class);
        list.addOnListChangedCallback(callback);
        source.removeAll();

        assertThat(list).isEmpty();
        verify(callback).onItemRangeRemoved(list, 0, 2);
    }
}
