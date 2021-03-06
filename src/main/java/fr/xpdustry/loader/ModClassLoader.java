package fr.xpdustry.loader;

import arc.struct.Seq;

public class ModClassLoader extends ClassLoader {

  private final Seq<ClassLoader> children = new Seq<>();
  private final ThreadLocal<Boolean> inChild = ThreadLocal.withInitial(() -> Boolean.FALSE);

  public ModClassLoader(ClassLoader parent) {
    super(parent);
  }

  public void addChild(ClassLoader child) {
    children.add(child);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    // A child may try to delegate class loading to its parent, which is *this class loader* - do not let that happen
    if (inChild.get()) {
      inChild.set(false);
      throw new ClassNotFoundException(name);
    }

    ClassNotFoundException last = null;
    int size = children.size;

    // If it doesn't exist in the main class loader, try all the children
    for (int i = 0; i < size; i++) {
      try {
        try {
          inChild.set(true);
          return children.get(i).loadClass(name);
        } finally {
          inChild.set(false);
        }
      } catch (final ClassNotFoundException e) {
        last = e;
      }
    }

    throw (last == null ? new ClassNotFoundException(name) : last);
  }
}
