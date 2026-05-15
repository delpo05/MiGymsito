package com.example.migymsito;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> importFinishedTrigger = new MutableLiveData<>();

    public void notifyImportFinished() {
        importFinishedTrigger.setValue(true);
    }

    public LiveData<Boolean> getImportFinishedTrigger() {
        return importFinishedTrigger;
    }
    
    public void resetImportFinishedTrigger() {
        importFinishedTrigger.setValue(false);
    }
}
