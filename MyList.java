class MyList {
    MyList next;
    int value; 
    public MyList (int n) {
        this.value = n;
        if (n > 0) {
            this.next = new MyList(n - 1);
        }
    }
    public String toString () {
        String ret = Integer.toString(value);
        if (next != null) {
            ret += " " + next.toString();
        }
        return ret;
    }
}
