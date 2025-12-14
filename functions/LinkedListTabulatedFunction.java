package functions;

import java.io.*;

public class LinkedListTabulatedFunction implements TabulatedFunction, Serializable {
    private class FunctionNode implements Serializable {
        private FunctionPoint point;
        private FunctionNode prev;
        private FunctionNode next;
        
        public FunctionNode(FunctionPoint point) {
            this.point = point;
        }
        
        public FunctionPoint getPoint() {
            return point;
        }
        
        public void setPoint(FunctionPoint point) {
            this.point = point;
        }
        
        public FunctionNode getPrev() {
            return prev;
        }
        
        public void setPrev(FunctionNode prev) {
            this.prev = prev;
        }
        
        public FunctionNode getNext() {
            return next;
        }
        
        public void setNext(FunctionNode next) {
            this.next = next;
        }
    }
    
    private FunctionNode head;
    private int pointsCount;
    private static final double EPSILON = 1e-10;
    
    public LinkedListTabulatedFunction() {
        initList();
    }
    
    public LinkedListTabulatedFunction(FunctionPoint[] points) throws IllegalArgumentException {
        if (points.length < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2, получено: " + points.length);
        }
        
        for (int i = 1; i < points.length; i++) {
            if (points[i].getX() < points[i - 1].getX() - EPSILON) {
                throw new IllegalArgumentException("Массив не упорядочен по координатам X");
            }
        }
        
        initList();
        this.pointsCount = points.length;
        
        for (FunctionPoint point : points) {
            addNodeToTail(new FunctionPoint(point));
        }
    }
    
    public LinkedListTabulatedFunction(double leftX, double rightX, int pointsCount) 
            throws IllegalArgumentException {
        
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница (" + leftX + 
                    ") должна быть меньше правой (" + rightX + ")");
        }
        
        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2, получено: " + pointsCount);
        }
        
        initList();
        this.pointsCount = pointsCount;
        
        double step = (rightX - leftX) / (pointsCount - 1);
        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            addNodeToTail(new FunctionPoint(x, 0));
        }
    }
    
    public LinkedListTabulatedFunction(double leftX, double rightX, double[] values) 
            throws IllegalArgumentException {
        
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница (" + leftX + 
                    ") должна быть меньше правой (" + rightX + ")");
        }
        
        if (values.length < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2, получено: " + values.length);
        }
        
        initList();
        this.pointsCount = values.length;
        
        double step = (rightX - leftX) / (values.length - 1);
        for (int i = 0; i < values.length; i++) {
            double x = leftX + i * step;
            addNodeToTail(new FunctionPoint(x, values[i]));
        }
    }
    
    private void initList() {
        head = new FunctionNode(null);
        head.setPrev(head);
        head.setNext(head);
        pointsCount = 0;
    }
    
    private FunctionNode getNodeByIndex(int index) throws FunctionPointIndexOutOfBoundsException {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException(index, 0, pointsCount - 1);
        }
        
        if (pointsCount == 0) {
            return head;
        }
        
        // Оптимизация: выбор направления обхода
        FunctionNode currentNode;
        if (index <= pointsCount / 2) {
            currentNode = head.getNext();
            for (int i = 0; i < index; i++) {
                currentNode = currentNode.getNext();
            }
        } else {
            currentNode = head.getPrev();
            for (int i = pointsCount - 1; i > index; i--) {
                currentNode = currentNode.getPrev();
            }
        }
        
        return currentNode;
    }
    
    private FunctionNode addNodeToTail(FunctionPoint point) {
        FunctionNode newNode = new FunctionNode(point);
        FunctionNode tail = head.getPrev();
        
        newNode.setPrev(tail);
        newNode.setNext(head);
        tail.setNext(newNode);
        head.setPrev(newNode);
        
        pointsCount++;
        return newNode;
    }
    
    private FunctionNode addNodeByIndex(int index, FunctionPoint point) 
            throws FunctionPointIndexOutOfBoundsException {
        
        if (index < 0 || index > pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException(index, 0, pointsCount);
        }
        
        if (index == pointsCount) {
            return addNodeToTail(point);
        }
        
        FunctionNode nodeAtIndex = getNodeByIndex(index);
        FunctionNode newNode = new FunctionNode(point);
        FunctionNode prevNode = nodeAtIndex.getPrev();
        
        newNode.setPrev(prevNode);
        newNode.setNext(nodeAtIndex);
        prevNode.setNext(newNode);
        nodeAtIndex.setPrev(newNode);
        
        pointsCount++;
        return newNode;
    }
    
    // Удаление узла по индексу
    private FunctionNode deleteNodeByIndex(int index) throws FunctionPointIndexOutOfBoundsException {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException(index, 0, pointsCount - 1);
        }
        
        FunctionNode nodeToDelete = getNodeByIndex(index);
        FunctionNode prevNode = nodeToDelete.getPrev();
        FunctionNode nextNode = nodeToDelete.getNext();
        
        prevNode.setNext(nextNode);
        nextNode.setPrev(prevNode);
        
        // Очищаем ссылки удаляемого узла
        nodeToDelete.setPrev(null);
        nodeToDelete.setNext(null);
        
        pointsCount--;
        return nodeToDelete;
    }
    
    @Override
    public double getLeftDomainBorder() {
        if (pointsCount == 0) {
            return Double.NaN;
        }
        return head.getNext().getPoint().getX();
    }
    
    @Override
    public double getRightDomainBorder() {
        if (pointsCount == 0) {
            return Double.NaN;
        }
        return head.getPrev().getPoint().getX();
    }
    
    @Override
    public double getFunctionValue(double x) {
        if (pointsCount == 0) {
            return Double.NaN;
        }
        
        double leftBorder = getLeftDomainBorder();
        double rightBorder = getRightDomainBorder();
        
        if (x < leftBorder - EPSILON || x > rightBorder + EPSILON) {
            return Double.NaN;
        }
        
        // Поиск интервала для интерполяции
        FunctionNode currentNode = head.getNext();
        while (currentNode != head) {
            FunctionNode nextNode = currentNode.getNext();
            if (nextNode == head) break; // Последний узел
            
            double x1 = currentNode.getPoint().getX();
            double x2 = nextNode.getPoint().getX();
            
            if (x >= x1 - EPSILON && x <= x2 + EPSILON) {
                // Точное совпадение
                if (Math.abs(x - x1) < EPSILON) {
                    return currentNode.getPoint().getY();
                }
                if (Math.abs(x - x2) < EPSILON) {
                    return nextNode.getPoint().getY();
                }
                
                // Линейная интерполяция
                double y1 = currentNode.getPoint().getY();
                double y2 = nextNode.getPoint().getY();
                return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
            }
            
            currentNode = nextNode;
        }
        
        return Double.NaN;
    }
    
    @Override
    public int getPointsCount() {
        return pointsCount;
    }
    
    @Override
    public FunctionPoint getPoint(int index) throws FunctionPointIndexOutOfBoundsException {
        FunctionNode node = getNodeByIndex(index);
        return new FunctionPoint(node.getPoint());
    }
    
    @Override
    public void setPoint(int index, FunctionPoint point) 
            throws FunctionPointIndexOutOfBoundsException, InappropriateFunctionPointException {
        
        FunctionNode node = getNodeByIndex(index);
        
        double newX = point.getX();
        double currentX = node.getPoint().getX();
        
        // Если X не изменился
        if (Math.abs(newX - currentX) < EPSILON) {
            node.getPoint().setY(point.getY());
            return;
        }
        
        // Проверка границ для новой координаты X
        FunctionNode prevNode = node.getPrev();
        FunctionNode nextNode = node.getNext();
        
        if (prevNode != head) {
            double prevX = prevNode.getPoint().getX();
            if (newX <= prevX + EPSILON) {
                throw new InappropriateFunctionPointException(
                    "Новая координата X (" + newX + ") должна быть больше предыдущей (" + prevX + ")");
            }
        }
        
        if (nextNode != head) {
            double nextX = nextNode.getPoint().getX();
            if (newX >= nextX - EPSILON) {
                throw new InappropriateFunctionPointException(
                    "Новая координата X (" + newX + ") должна быть меньше следующей (" + nextX + ")");
            }
        }
        
        // Устанавливаем новые координаты
        node.setPoint(new FunctionPoint(point));
    }
    
    @Override
    public double getPointX(int index) throws FunctionPointIndexOutOfBoundsException {
        return getNodeByIndex(index).getPoint().getX();
    }
    
    @Override
    public void setPointX(int index, double x) 
            throws FunctionPointIndexOutOfBoundsException, InappropriateFunctionPointException {
        
        FunctionNode node = getNodeByIndex(index);
        
        double currentX = node.getPoint().getX();
        if (Math.abs(x - currentX) < EPSILON) {
            return; // X не изменился
        }
        
        // Проверка границ
        FunctionNode prevNode = node.getPrev();
        FunctionNode nextNode = node.getNext();
        
        if (prevNode != head) {
            double prevX = prevNode.getPoint().getX();
            if (x <= prevX + EPSILON) {
                throw new InappropriateFunctionPointException(
                    "Новая координата X (" + x + ") должна быть больше предыдущей (" + prevX + ")");
            }
        }
        
        if (nextNode != head) {
            double nextX = nextNode.getPoint().getX();
            if (x >= nextX - EPSILON) {
                throw new InappropriateFunctionPointException(
                    "Новая координата X (" + x + ") должна быть меньше следующей (" + nextX + ")");
            }
        }
        
        node.getPoint().setX(x);
    }
    
    @Override
    public double getPointY(int index) throws FunctionPointIndexOutOfBoundsException {
        return getNodeByIndex(index).getPoint().getY();
    }
    
    @Override
    public void setPointY(int index, double y) throws FunctionPointIndexOutOfBoundsException {
        getNodeByIndex(index).getPoint().setY(y);
    }
    
    @Override
    public void deletePoint(int index) 
            throws FunctionPointIndexOutOfBoundsException, IllegalStateException {
        
        if (pointsCount <= 2) {
            throw new IllegalStateException("Невозможно удалить точку: минимальное количество точек - 2");
        }
        
        deleteNodeByIndex(index);
    }
    
    @Override
    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        if (pointsCount == 0) {
            // Первая точка
            addNodeToTail(new FunctionPoint(point));
            return;
        }
        
        double newX = point.getX();
        double leftBorder = getLeftDomainBorder();
        double rightBorder = getRightDomainBorder();
        
        // Проверка, куда добавлять
        if (newX < leftBorder - EPSILON) {
            // Добавляем в начало
            addNodeByIndex(0, new FunctionPoint(point));
            return;
        }
        
        if (newX > rightBorder + EPSILON) {
            // Добавляем в конец
            addNodeToTail(new FunctionPoint(point));
            return;
        }
        
        // Поиск позиции для вставки
        int insertIndex = 0;
        FunctionNode currentNode = head.getNext();
        
        while (currentNode != head && 
               newX > currentNode.getPoint().getX() + EPSILON) {
            currentNode = currentNode.getNext();
            insertIndex++;
        }
        
        // Проверка на дублирование
        if (currentNode != head && 
            Math.abs(newX - currentNode.getPoint().getX()) < EPSILON) {
            throw new InappropriateFunctionPointException(
                "Точка с координатой X = " + newX + " уже существует");
        }
        
        // Вставка
        addNodeByIndex(insertIndex, new FunctionPoint(point));
    }
    
    public void printPoints() {
        System.out.println("Табулированная функция (связный список, " + pointsCount + " точек):");
        
        if (pointsCount == 0) {
            System.out.println("  Функция не содержит точек");
            return;
        }
        
        FunctionNode currentNode = head.getNext();
        int index = 0;
        
        while (currentNode != head) {
            double x = currentNode.getPoint().getX();
            double y = currentNode.getPoint().getY();
            System.out.printf("  [%d]: (%.4f, %.4f)%n", index, x, y);
            currentNode = currentNode.getNext();
            index++;
        }
    }
    
    // Методы сериализации
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(pointsCount);
        
        FunctionNode current = head.getNext();
        while (current != head) {
            out.writeDouble(current.getPoint().getX());
            out.writeDouble(current.getPoint().getY());
            current = current.getNext();
        }
    }
    
    private void readObject(java.io.ObjectInputStream in) 
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        initList();
        
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            double x = in.readDouble();
            double y = in.readDouble();
            addNodeToTail(new FunctionPoint(x, y));
        }
    }
}