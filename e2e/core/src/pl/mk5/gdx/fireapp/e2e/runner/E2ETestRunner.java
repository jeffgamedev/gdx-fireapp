package pl.mk5.gdx.fireapp.e2e.runner;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public interface E2ETestRunner {

    void addNext(Class<? extends E2ETest> testType) throws ReflectionException;

    void addNext(Class<? extends E2ETest> testType, float durationSeconds) throws ReflectionException;

    void start();

    void render(Batch batch);

    void onFinish(Runnable runnable);

    void only(Class<? extends E2ETest>... testType);
}
