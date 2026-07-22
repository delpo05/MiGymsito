package com.example.migymsito;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RegistrationViewModel extends ViewModel {
    public final MutableLiveData<String> nombre = new MutableLiveData<>("");
    public final MutableLiveData<String> correo = new MutableLiveData<>("");
    public final MutableLiveData<Long> fechaNacimiento = new MutableLiveData<>(0L);
    public final MutableLiveData<String> genero = new MutableLiveData<>("");
    public final MutableLiveData<Double> peso = new MutableLiveData<>(0.0);
    public final MutableLiveData<Double> altura = new MutableLiveData<>(0.0);
    
    public final MutableLiveData<String> fechaStr = new MutableLiveData<>("");
    public final MutableLiveData<String> pesoStr = new MutableLiveData<>("");
    public final MutableLiveData<String> alturaStr = new MutableLiveData<>("");
}
