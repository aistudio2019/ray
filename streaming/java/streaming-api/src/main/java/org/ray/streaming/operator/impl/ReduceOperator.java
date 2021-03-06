package org.ray.streaming.operator.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ray.streaming.api.collector.Collector;
import org.ray.streaming.api.context.RuntimeContext;
import org.ray.streaming.api.function.impl.ReduceFunction;
import org.ray.streaming.message.KeyRecord;
import org.ray.streaming.message.Record;
import org.ray.streaming.operator.OneInputOperator;
import org.ray.streaming.operator.StreamOperator;

public class ReduceOperator<K, T> extends StreamOperator<ReduceFunction<T>> implements
    OneInputOperator<T> {

  private Map<K, T> reduceState;

  public ReduceOperator(ReduceFunction<T> reduceFunction) {
    super(reduceFunction);
  }

  @Override
  public void open(List<Collector> collectorList, RuntimeContext runtimeContext) {
    super.open(collectorList, runtimeContext);
    this.reduceState = new HashMap<>();
  }

  @Override
  public void processElement(Record<T> record) throws Exception {
    KeyRecord<K, T> keyRecord = (KeyRecord<K, T>) record;
    K key = keyRecord.getKey();
    T value = keyRecord.getValue();
    if (reduceState.containsKey(key)) {
      T oldValue = reduceState.get(key);
      T newValue = this.function.reduce(oldValue, value);
      reduceState.put(key, newValue);
      collect(new Record(newValue));
    } else {
      reduceState.put(key, value);
      collect(record);
    }
  }
}
