package at.ac.tuwien.otl.ss18.pwlk.util;

import java.util.Objects;

public class Pair<T, E> {

  private final T key;
  private final E value;

  public Pair(final T key, final E value) {
    this.key = key;
    this.value = value;
  }

  public T getKey() {
    return key;
  }

  public E getValue() {
    return value;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equals(key, pair.key) &&
            Objects.equals(value, pair.value);
  }

  @Override
  public int hashCode() {

    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Pair{");
    sb.append("key=").append(key);
    sb.append(", value=").append(value);
    sb.append('}');
    return sb.toString();
  }
}
