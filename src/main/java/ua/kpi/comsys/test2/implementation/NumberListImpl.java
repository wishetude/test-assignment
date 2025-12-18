/*
 * Copyright (c) 2014, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * Author: Каленіченко Варвара Андріївна, Група: ІС-33, № заліковки: 7
 */

package ua.kpi.comsys.test2.implementation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import ua.kpi.comsys.test2.NumberList;

/**
 * Реалізація списку для зберігання чисел у вісімковій системі числення.
 * Кожен елемент списку містить одну цифру (0-7).
 *
 * Параметри варіанту:
 * - C3 = 7 mod 3 = 1 -> Кільцевий однонаправлений список
 * - C5 = 7 mod 5 = 2 -> Вісімкова система (цифри 0-7)
 * - C7 = 7 mod 7 = 0 -> Операція додавання
 * - Додаткова система: (2+1) mod 5 = 3 -> Десяткова
 *
 * @author Каленіченко Варвара Андріївна
 */
public class NumberListImpl implements NumberList {

    private static final int DEFAULT_BASE = 8; // вісімкова система (для варіанту 7)
    private static final int ALTERNATIVE_BASE = 10; // десяткова система

    private final int base; // система числення для цього списку
    private Node head; // голова списку
    private Node tail; // хвіст списку
    private int size; // розмір списку
    private int modCount = 0; // лічильник модифікацій для ітераторів

    // Вузол кільцевого однонаправленого списку
    private static class Node {
        byte data; // цифра
        Node next; // посилання на наступний вузол

        Node(byte data) {
            this.data = data;
        }
    }

    /**
     * Default constructor. Returns empty <tt>NumberListImpl</tt>
     */
    public NumberListImpl() {
        this.base = DEFAULT_BASE;
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Constructor with custom base.
     */
    private NumberListImpl(int base) {
        this.base = base;
        head = null;
        tail = null;
        size = 0;
    }


    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * from file, defined in string format.
     *
     * @param file - file where number is stored.
     */
    public NumberListImpl(File file) {
        this();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                initFromDecimalString(line.trim());
            }
        } catch (Exception e) {
            // якщо файл не знайдено або помилка читання - залишаємо список порожнім
        }
    }


    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * in string notation.
     *
     * @param value - number in string notation.
     */
    public NumberListImpl(String value) {
        this();
        try {
            initFromDecimalString(value);
        } catch (Exception e) {
            // якщо рядок некоректний - залишаємо список порожнім
        }
    }

    // Ініціалізація списку з десяткового числа (у вигляді рядка)
    private void initFromDecimalString(String decimalStr) {
        if (decimalStr == null || decimalStr.isEmpty()) {
            return;
        }

        // прибираємо нулі на початку
        decimalStr = decimalStr.replaceFirst("^0+(?!$)", "");

        // перетворюємо рядок у число (використовуємо BigInteger для великих чисел)
        BigInteger decimalValue;
        try {
            decimalValue = new BigInteger(decimalStr);
        } catch (NumberFormatException e) {
            return; // якщо число некоректне, залишаємо список порожнім
        }

        // перевіряємо на від'ємні числа
        if (decimalValue.compareTo(BigInteger.ZERO) < 0) {
            return;
        }

        if (decimalValue.equals(BigInteger.ZERO)) {
            add((byte) 0);
            return;
        }

        // переводимо десяткове число у нашу систему числення
        String numberInBase = decimalValue.toString(base);
        for (int i = 0; i < numberInBase.length(); i++) {
            char ch = numberInBase.charAt(i);
            byte digit;
            if (ch >= '0' && ch <= '9') {
                digit = (byte) (ch - '0');
            } else if (ch >= 'a' && ch <= 'z') {
                digit = (byte) (ch - 'a' + 10);
            } else if (ch >= 'A' && ch <= 'Z') {
                digit = (byte) (ch - 'A' + 10);
            } else {
                continue;
            }
            add(digit);
        }
    }


    /**
     * Saves the number, stored in the list, into specified file
     * in <b>decimal</b> scale of notation.
     *
     * @param file - file where number has to be stored.
     */
    public void saveList(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(toDecimalString());
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + e.getMessage(), e);
        }
    }


    /**
     * Returns student's record book number, which has 4 decimal digits.
     *
     * @return student's record book number.
     */
    public static int getRecordBookNumber() {
        return 7;
    }


    /**
     * Переведення числа з вісімкової у десяткову систему числення.
     * Не змінює поточний список.
     *
     * @return новий список у десятковій системі
     */
    public NumberListImpl changeScale() {
        // отримуємо десяткове представлення числа
        String decimalValue = toDecimalString();

        // створюємо новий список в альтернативній системі числення
        NumberListImpl result = new NumberListImpl(ALTERNATIVE_BASE);

        if (decimalValue.equals("0")) {
            result.add((byte) 0);
            return result;
        }

        // переводимо десяткове число у альтернативну систему
        BigInteger number = new BigInteger(decimalValue);
        String numberInAltBase = number.toString(ALTERNATIVE_BASE);

        for (int i = 0; i < numberInAltBase.length(); i++) {
            char ch = numberInAltBase.charAt(i);
            byte digit;
            if (ch >= '0' && ch <= '9') {
                digit = (byte) (ch - '0');
            } else if (ch >= 'a' && ch <= 'z') {
                digit = (byte) (ch - 'a' + 10);
            } else if (ch >= 'A' && ch <= 'Z') {
                digit = (byte) (ch - 'A' + 10);
            } else {
                continue;
            }
            result.add(digit);
        }

        return result;
    }


    /**
     * Додавання двох чисел у вісімковій системі числення.
     * Не змінює поточний список та аргумент.
     *
     * @param arg - друге число для додавання
     * @return результат додавання
     */
    public NumberListImpl additionalOperation(NumberList arg) {
        if (arg == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }

        NumberListImpl result = new NumberListImpl(this.base);

        int carry = 0; // перенос у наступний розряд
        int i = this.size() - 1; // індекс для першого числа
        int j = arg.size() - 1; // індекс для другого числа

        // додаємо цифри справа наліво, як у стовпчик
        while (i >= 0 || j >= 0 || carry > 0) {
            int digit1 = i >= 0 ? this.get(i) : 0;
            int digit2 = j >= 0 ? arg.get(j) : 0;

            int sum = digit1 + digit2 + carry;
            carry = sum / base; // перенос
            int resultDigit = sum % base; // цифра результату

            result.add(0, (byte) resultDigit); // додаємо на початок

            i--;
            j--;
        }

        return result;
    }


    /**
     * Повертає число у десятковій системі як рядок.
     *
     * @return рядок з числом у десятковій системі
     */
    public String toDecimalString() {
        if (isEmpty()) {
            return "0";
        }

        // переводимо з нашої системи у десяткову
        BigInteger decimal = BigInteger.ZERO;
        BigInteger baseValue = BigInteger.valueOf(base);

        Node current = head;
        for (int i = 0; i < size; i++) {
            decimal = decimal.multiply(baseValue).add(BigInteger.valueOf(current.data));
            current = current.next;
        }

        return decimal.toString();
    }


    @Override
    public String toString() {
        if (isEmpty()) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        Node current = head;
        for (int i = 0; i < size; i++) {
            sb.append(current.data);
            current = current.next;
        }

        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberList)) return false;

        NumberList other = (NumberList) o;

        if (this.size() != other.size()) return false;

        for (int i = 0; i < size; i++) {
            if (!this.get(i).equals(other.get(i))) {
                return false;
            }
        }

        return true;
    }


    @Override
    public int size() {
        return size;
    }


    @Override
    public boolean isEmpty() {
        return size == 0;
    }


    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Byte)) {
            return false;
        }

        // перевіряємо чи є такий елемент у списку
        Node current = head;
        for (int i = 0; i < size; i++) {
            if (current.data == (Byte) o) {
                return true;
            }
            current = current.next;
        }

        return false;
    }


    @Override
    public Iterator<Byte> iterator() {
        return new NumberListIterator();
    }


    @Override
    public Object[] toArray() {
        // перетворюємо список у масив
        Object[] array = new Object[size];
        Node current = head;
        for (int i = 0; i < size; i++) {
            array[i] = current.data;
            current = current.next;
        }
        return array;
    }


    @Override
    public <T> T[] toArray(T[] a) {
        // This method is not required to be implemented per assignment
        return null;
    }


    @Override
    public boolean add(Byte e) {
        if (e == null) {
            throw new NullPointerException("Null elements not permitted");
        }
        if (e < 0 || e >= base) {
            throw new IllegalArgumentException("Digit must be in range [0, " + (base - 1) + "]");
        }

        Node newNode = new Node(e);

        if (isEmpty()) {
            // якщо список порожній, створюємо перший елемент
            head = newNode;
            tail = newNode;
            newNode.next = head; // замикаємо в кільце
        } else {
            // додаємо в кінець і підтримуємо кільцеву структуру
            tail.next = newNode;
            tail = newNode;
            tail.next = head;
        }

        size++;
        modCount++;
        return true;
    }


    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Byte)) {
            return false;
        }

        if (isEmpty()) {
            return false;
        }

        byte target = (Byte) o;

        // видаляємо голову списку
        if (head.data == target) {
            if (size == 1) {
                head = null;
                tail = null;
            } else {
                head = head.next;
                tail.next = head; // підтримуємо кільце
            }
            size--;
            modCount++;
            return true;
        }

        // шукаємо елемент для видалення
        Node current = head;
        for (int i = 0; i < size - 1; i++) {
            if (current.next.data == target) {
                Node toRemove = current.next;
                current.next = toRemove.next;
                if (toRemove == tail) {
                    tail = current;
                }
                size--;
                modCount++;
                return true;
            }
            current = current.next;
        }

        return false;
    }


    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        if (c == null || c.isEmpty()) {
            return false;
        }

        boolean modified = false;
        for (Byte b : c) {
            add(b);
            modified = true;
        }
        return modified;
    }


    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        if (c == null || c.isEmpty()) {
            return false;
        }

        for (Byte b : c) {
            add(index++, b);
        }
        return true;
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }

        boolean modified = false;
        Iterator<Byte> it = iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }


    @Override
    public boolean retainAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }

        boolean modified = false;
        Iterator<Byte> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }


    @Override
    public void clear() {
        head = null;
        tail = null;
        size = 0;
        modCount++;
    }


    @Override
    public Byte get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        // проходимо список до потрібного індексу
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        return current.data;
    }


    @Override
    public Byte set(int index, Byte element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (element == null) {
            throw new NullPointerException("Null elements not permitted");
        }
        if (element < 0 || element >= base) {
            throw new IllegalArgumentException("Digit must be in range [0, " + (base - 1) + "]");
        }

        // знаходимо потрібний елемент
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        // міняємо значення і повертаємо старе
        byte oldValue = current.data;
        current.data = element;
        modCount++;

        return oldValue;
    }


    @Override
    public void add(int index, Byte element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (element == null) {
            throw new NullPointerException("Null elements not permitted");
        }
        if (element < 0 || element >= base) {
            throw new IllegalArgumentException("Digit must be in range [0, " + (base - 1) + "]");
        }

        if (index == size) {
            add(element);
            return;
        }

        Node newNode = new Node(element);

        if (index == 0) {
            // вставка на початок
            if (isEmpty()) {
                head = newNode;
                tail = newNode;
                newNode.next = head;
            } else {
                newNode.next = head;
                head = newNode;
                tail.next = head;
            }
        } else {
            // вставка в середину
            Node current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            newNode.next = current.next;
            current.next = newNode;
        }

        size++;
        modCount++;
    }


    @Override
    public Byte remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        byte removed;

        if (index == 0) {
            // видалення голови
            removed = head.data;
            if (size == 1) {
                head = null;
                tail = null;
            } else {
                head = head.next;
                tail.next = head;
            }
        } else {
            // видалення з середини або кінця
            Node current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            removed = current.next.data;
            current.next = current.next.next;
            if (index == size - 1) {
                tail = current;
            }
        }

        size--;
        modCount++;
        return removed;
    }


    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Byte)) {
            return -1;
        }

        // шукаємо перше входження елемента
        byte target = (Byte) o;
        Node current = head;
        for (int i = 0; i < size; i++) {
            if (current.data == target) {
                return i;
            }
            current = current.next;
        }

        return -1;
    }


    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof Byte)) {
            return -1;
        }

        // шукаємо останнє входження елемента
        byte target = (Byte) o;
        int lastIndex = -1;
        Node current = head;
        for (int i = 0; i < size; i++) {
            if (current.data == target) {
                lastIndex = i;
            }
            current = current.next;
        }

        return lastIndex;
    }


    @Override
    public ListIterator<Byte> listIterator() {
        return new NumberListListIterator(0);
    }


    @Override
    public ListIterator<Byte> listIterator(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return new NumberListListIterator(index);
    }


    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + ", toIndex: " + toIndex + ", size: " + size);
        }

        NumberListImpl subList = new NumberListImpl();
        for (int i = fromIndex; i < toIndex; i++) {
            subList.add(get(i));
        }

        return subList;
    }


    @Override
    public boolean swap(int index1, int index2) {
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) {
            return false;
        }

        if (index1 == index2) {
            return true;
        }

        // міняємо місцями елементи
        Byte temp = get(index1);
        set(index1, get(index2));
        set(index2, temp);

        return true;
    }


    @Override
    public void sortAscending() {
        if (size <= 1) {
            return;
        }

        // сортування бульбашкою за зростанням
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - i - 1; j++) {
                if (get(j) > get(j + 1)) {
                    swap(j, j + 1);
                }
            }
        }
    }


    @Override
    public void sortDescending() {
        if (size <= 1) {
            return;
        }

        // сортування бульбашкою за спаданням
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - i - 1; j++) {
                if (get(j) < get(j + 1)) {
                    swap(j, j + 1);
                }
            }
        }
    }


    @Override
    public void shiftLeft() {
        if (size <= 1) {
            return;
        }

        // циклічний зсув ліворуч - перший елемент стає останнім
        byte first = head.data;
        head = head.next;
        tail.data = first;
        tail = tail.next;

        modCount++;
    }


    @Override
    public void shiftRight() {
        if (size <= 1) {
            return;
        }

        byte last = tail.data;

        // знаходимо передостанній вузол
        Node current = head;
        for (int i = 0; i < size - 2; i++) {
            current = current.next;
        }

        tail = current;

        // створюємо нову голову зі значенням останнього елемента
        Node newHead = new Node(last);
        newHead.next = head;
        head = newHead;
        tail.next = head;

        modCount++;
    }

    // Ітератор для проходу по списку
    private class NumberListIterator implements Iterator<Byte> {
        private Node current;
        private Node lastReturned;
        private int position;
        private int expectedModCount;

        NumberListIterator() {
            current = head;
            lastReturned = null;
            position = 0;
            expectedModCount = modCount;
        }

        @Override
        public boolean hasNext() {
            return position < size;
        }

        @Override
        public Byte next() {
            checkForComodification();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            lastReturned = current;
            current = current.next;
            position++;
            return lastReturned.data;
        }

        @Override
        public void remove() {
            checkForComodification();
            if (lastReturned == null) {
                throw new IllegalStateException();
            }

            // видаляємо останній повернутий елемент
            NumberListImpl.this.remove((Object) lastReturned.data);
            lastReturned = null;
            expectedModCount = modCount;
        }

        private void checkForComodification() {
            // перевіряємо чи не змінили список під час ітерації
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    // ListIterator з можливістю руху в обидва боки
    private class NumberListListIterator implements ListIterator<Byte> {
        private Node current;
        private Node lastReturned;
        private int position;
        private int expectedModCount;

        NumberListListIterator(int index) {
            current = head;
            for (int i = 0; i < index && current != null; i++) {
                current = current.next;
            }
            lastReturned = null;
            position = index;
            expectedModCount = modCount;
        }

        @Override
        public boolean hasNext() {
            return position < size;
        }

        @Override
        public Byte next() {
            checkForComodification();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            lastReturned = current;
            current = current.next;
            position++;
            return lastReturned.data;
        }

        @Override
        public boolean hasPrevious() {
            return position > 0;
        }

        @Override
        public Byte previous() {
            checkForComodification();
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }

            position--;
            Node prev = getNodeAt(position);
            lastReturned = prev;
            current = prev;
            return lastReturned.data;
        }

        @Override
        public int nextIndex() {
            return position;
        }

        @Override
        public int previousIndex() {
            return position - 1;
        }

        @Override
        public void remove() {
            checkForComodification();
            if (lastReturned == null) {
                throw new IllegalStateException();
            }

            NumberListImpl.this.remove(position - 1);
            position--;
            lastReturned = null;
            expectedModCount = modCount;
        }

        @Override
        public void set(Byte e) {
            checkForComodification();
            if (lastReturned == null) {
                throw new IllegalStateException();
            }

            NumberListImpl.this.set(position - 1, e);
            expectedModCount = modCount;
        }

        @Override
        public void add(Byte e) {
            checkForComodification();
            NumberListImpl.this.add(position, e);
            position++;
            lastReturned = null;
            expectedModCount = modCount;
        }

        private Node getNodeAt(int index) {
            // отримуємо вузол за індексом
            Node node = head;
            for (int i = 0; i < index; i++) {
                node = node.next;
            }
            return node;
        }

        private void checkForComodification() {
            // перевіряємо чи не змінили список під час ітерації
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
