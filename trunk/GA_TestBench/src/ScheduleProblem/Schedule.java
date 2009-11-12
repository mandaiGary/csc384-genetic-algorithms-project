package ScheduleProblem;

import ga_testbench.Individual;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

/**
 * This is the big daddy class. It keeps static lists of students and rooms
 * which it asks to evaluate itself when it needs to be evaluated.
 */
public class Schedule extends ga_testbench.Individual implements Cloneable {

    private static List<Student> students;
    private static List<Room> rooms;
    private static List<Course> courses;
    private static int numDays;
    private static int numTimes;
    private static int numCourses;
    private static int numRooms;
    private static int numStudents;
    private static boolean initialized;
    private static Random rand;
    private static int hillEvals = 0;

    /**
     * This will be called once by the solver. It has to load the instance info
     * and make the static members.
     * @param filename File from which to load instance data
     * @return True if initialization went smoothly, false on error
     */
    public static boolean initialize(String filename) {
        if (initialized) {
            throw new RuntimeException("Schedule is already initialized. " +
                    "Cannot be initialized twice.");
        }

        File infile = new File(filename);
        Scanner scanner;
        try {
            scanner = new Scanner(infile);
        } catch (FileNotFoundException ex) {
            return false;
        }

        try {
            numDays = scanner.nextInt();
            numTimes = scanner.nextInt();

            // Collect the courses from the file
            numCourses = scanner.nextInt();
            courses = new ArrayList<Course>(numCourses);
            scanner.nextLine(); // Skip line after getting an int
            for (int i = 0; i < numCourses; i++) {
                String name = scanner.nextLine();
                Course c = new Course(name);
                courses.add(c);
            }

            // Collect the rooms from the file
            numRooms = scanner.nextInt();
            rooms = new ArrayList<Room>(numRooms);
            scanner.nextLine(); // Skip line after getting an int
            for (int i = 0; i < numRooms; i++) {
                String name = scanner.nextLine();
                Room c = new Room(name);
                rooms.add(c);
            }

            // Collect the students from the file
            numStudents = scanner.nextInt();
            students = new ArrayList<Student>(numStudents);
            scanner.nextLine(); // Skip line after getting an int
            for (int i = 0; i < numStudents; i++) {
                String name = scanner.nextLine();

                int cnum = scanner.nextInt();
                scanner.nextLine(); // Skip line after getting an int
                List<Course> studentCourses = new ArrayList<Course>(cnum);
                for (int j = 0; j < cnum; j++) {
                    String courseName = scanner.nextLine();
                    studentCourses.add(getCourse(courseName));
                }
                Student stud = new Student(name, studentCourses);
                students.add(stud);
            }
        } catch (NoSuchElementException e) {
            return false;
        }

        rand = new Random();
        initialized = true;
        scanner.close();

        return true;
    }

    public static int getNumCourses() {
        verifyInitialized();
        return numCourses;
    }

    public static int getNumDays() {
        verifyInitialized();
        return numDays;
    }

    public static int getNumTimes() {
        return numTimes;
    }

    public static int getNumRooms() {
        verifyInitialized();
        return numRooms;
    }

    public static int getNumStudents() {
        verifyInitialized();
        return numStudents;
    }

    /**
     * Dump the instance info to the screen in a nice way
     */
    static void displayInfo() {
        verifyInitialized();

        System.out.println("The " + numCourses + " courses in this problem instance are:");
        for (Course course : courses) {
            System.out.println(course.getName());
        }
        System.out.println("The " + numRooms + " rooms in this problem instance are:");
        for (Room room : rooms) {
            System.out.println(room.getName());
        }
        System.out.println("The " + numStudents + " students in this problem instance are:");
        for (Student student : students) {
            List<Course> studCourses = student.getCourses();
            System.out.println(student.getName() + ", who takes these " + studCourses.size() + " classes:");
            for (Course c : studCourses) {
                System.out.println(" " + c.getName());
            }
        }
    }

    /**
     * Generate a random schedule
     * @return A random Schedule
     */
    public static Individual random() {
        verifyInitialized();

        Timing[] times = new Timing[numCourses];
        int[] timingRooms = new int[numCourses];
        for (int i = 0; i < numCourses; i++) {
            int day = rand.nextInt(numDays);
            int time = rand.nextInt(numTimes);
            int room = rand.nextInt(numRooms);
            Timing t = new Timing(day, time);
            times[i] = t;
            timingRooms[i] = room;
        }

        return new Schedule(times, timingRooms);
    }

    /**
     * Get a particular course from the scheduling problem instance
     * @param courseName The name of the course to get
     * @return The course whose name is courseName
     */
    static Course getCourse(String courseName) {
        for (Course course : courses) {
            if (course.getName().equals(courseName)) {
                return course;
            }
        }
        throw new RuntimeException(courseName + "is an unrecognized course name.");
    }

    static List<Course> getCourses() {
        verifyInitialized();
        return courses;
    }

    static List<Room> getRooms() {
        verifyInitialized();
        return rooms;
    }

    static List<Student> getStudents() {
        verifyInitialized();
        return students;
    }

    private static void verifyInitialized() {
        if (!initialized) {
            throw new RuntimeException("Schedule not initialized.");
        }
    }
    /****************************
     *                          *
     * Start non-static portion *
     *                          *
     ****************************/
    /**
     * The times that the different courses occur at, indexed by course number.
     */
    private Timing[] times;
    /**
     * The rooms assigned to the different courses, indexed by course number.
     */
    private int[] timingRooms;

    /**
     * Constructor for Schedule
     * @param times The Timings that the courses occur at
     */
    public Schedule(Timing[] times, int[] timingRooms) {
        verifyInitialized();

        if (times.length != numCourses || timingRooms.length != numCourses) {
            throw new RuntimeException("Given arrays have wrong size.");
        }
        this.times = times;
        this.timingRooms = timingRooms;
    }

    // THIS MUST BE DONE
    @Override
//    protected Object clone() throws CloneNotSupportedException {
//        Timing[] newTimes = times.clone();
//        return super.clone();
//
//        /* This is totally incomplete
//         */
//    }
    protected Schedule clone() throws CloneNotSupportedException {
        Timing[] newTimes = this.times.clone();
        int[] newTimingRooms = this.timingRooms.clone();
        Schedule newSchedule = new Schedule(newTimes, newTimingRooms);

        return newSchedule;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Schedule other = (Schedule) obj;
        if (!Arrays.deepEquals(this.times, other.times)) {
            return false;
        }
        if (!Arrays.equals(this.timingRooms, other.timingRooms)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Arrays.deepHashCode(this.times);
        hash = 41 * hash + Arrays.hashCode(this.timingRooms);
        return hash;
    }

    /**
     * Get the overall fitness of this schedule
     * @return The fitness of this schedule
     */
    @Override
    public double fitness() {
        double ret = 0;
        double studentRating = 0.0f;
        double roomRating = 0.0f;
        for (Student student : students) {
            studentRating += student.evaluate(this);
        }
        for (Room room : rooms) {
            roomRating += room.evaluate(this);
        }

        // We care twice as much about students as we do about rooms
        ret = 2 * studentRating / numStudents + roomRating / numRooms;

        return ret;
    }

    /**
     * Mutate a schedule.
     * @param difference The probability that any part of the Schedule will change.
     * @return A new mutated schedule.
     */
    @Override
    public Individual mutate(double difference) {
        Timing[] t = new Timing[numCourses];
        int[] tr = new int[numCourses];

        for (int i = 0; i < numCourses; i++) {
            if (rand.nextDouble() < difference) {
                tr[i] = rand.nextInt(numRooms);
            } else {
                tr[i] = timingRooms[i];
            }
            if (rand.nextDouble() < difference) {
                int day = rand.nextInt(numDays);
                int time = rand.nextInt(numTimes);
                t[i] = new Timing(day, time);
            } else {
                t[i] = this.times[i];
            }
        }
        Schedule ret = new Schedule(t, tr);
        return ret;
    }

    /**
     * Breed a new schedule from a pair of other schedules.
     * @param other The schedule to cross with.
     * @return A new Schedule with all of its parts taken from one of the input
     * schedule
     */
    @Override
    public Individual crossWith(Individual other) {
        if (other == null || getClass() != other.getClass()) {
            throw new RuntimeException("Must cross with a Schedule");
        }
        final Schedule otherSched = (Schedule) other;
        /*
        // Very cheesy crossover technique
        return new Schedule(times, otherSched.timingRooms);
         */

        /* Alternative, poorer technique */
        Schedule ret = null;


        try {
            ret = this.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Not cloneable. Odd.");
        }

        // Very cheesy crossover technique
        for (int i = 0; i < numCourses; i++) {
            boolean useOther = rand.nextDouble() > 0.5f;
            if (useOther) {
                ret.timingRooms[i] = otherSched.timingRooms[i];
                ret.times[i] = otherSched.times[i];
            }
        }
        return ret;


    }

    /**
     * Get the exam time of some course
     * @param course The course to find the exam time for
     * @return The Timing that the course's exam occurs at
     */
    Timing getTime(Course course) {
        int index;
        index = courses.indexOf(course);
        if (index == -1) {
            throw new RuntimeException("Unknown Course found.");
        }
        return this.times[index];
    }

    /**
     * Get the times that some room has an exam in it
     * @param room The room to find the TimeTable for
     * @return The TimeTable for the Timings where the room has an exam
     * scheduled in it
     */
    TimeTable getTimes(Room room) {
        TimeTable ret = new TimeTable();
        int index = rooms.indexOf(room);
        for (int i = 0; i < numCourses; i++) {
            if (timingRooms[i] == index) {
                ret.addTime(this.times[i]);
            }
        }
        return ret;
    }

    /**
     * Get the timetable that a student has
     * @param student A student
     * @return The timetable for the courses that the student is enrolled in
     */
    TimeTable getTimes(Student student) {
        // Get each of the timing for the student's courses
        TimeTable ret = new TimeTable();
        for (Course c : student.getCourses()) {
            ret.addTime(c.getTiming(this));
        }
        return ret;
    }

    /**
     * Give a nicely formatted string showing the exam schedules of all of
     * the students.
     * @return String representation of every student's schedule.
     */
    public String studentSchedulesString() {
        String ret = "";
        for (Student s : students) {
            ret += s.getName() + "'s schedule (which (s)he rates at " + s.evaluate(this) + "):\n";
            ret += s.getTimeTable(this);
            ret += "\n";
        }
        return ret;
    }

    /**
     * Give a nicely formatted string showing the exam schedules of all of
     * the rooms.
     * @return String representation of every student's schedule.
     */
    public String roomSchedulesString() {
        String ret = "";
        for (Room s : rooms) {
            ret += s.getName() + "'s schedule (which it rates at " + s.evaluate(this) + "):\n";
            ret += s.getTimeTable(this);
            ret += "\n";
        }
        return ret;
    }

    /**
     * Get a String representing all of the exam tines and rooms.
     * @return
     */
    @Override
    public String toString() {
        String ret = "Schedule information:\n";
        for (int i = 0; i < numCourses; i++) {
            ret += courses.get(i).getName() + " has exam in " + rooms.get(timingRooms[i]).getName() + " at " + times[i] + "\n";
        }
        return ret;
    }

    /**
     * Check how many evaluations were required for local hill climbing, and
     * reset hillEvals to 0.
     * @return The number of evaluations berformed by a call to hillClimb
     */
    public static int getHillEvals() {
        int ret = hillEvals;
        hillEvals = 0;
        return ret;
    }

    /**
     * HillClimb from this Schedule to find the best one.
     * @return A locally optimal schedule
     * @throws CloneNotSupportedException
     */
    public Schedule hillClimb(int maxEvals) throws CloneNotSupportedException {
        Schedule best = this;
        double fitness = fitness();
        for (int i = 0; i < numCourses; i++) {
            for (int j = 0; j < numRooms; j++) {
                Schedule sched = this.clone();
                // switch room[i] to j
                if (sched.timingRooms[i] != j) {
                    sched.timingRooms[i] = j;
                    double newfit = sched.fitness();
                    hillEvals++;
                    if (newfit > fitness) {
                        fitness = newfit;
                        best = sched;
                    }
                    if (hillEvals >= maxEvals) {
                        return best;
                    }
                }
            }
            for (int j = 0; j < numDays; j++) {
                Schedule sched = this.clone();
                if (sched.times[i].getDay() != j) {
                    sched.times[i] = new Timing(j, sched.times[i].getTime());
                    double newfit = sched.fitness();
                    hillEvals++;
                    if (newfit > fitness) {
                        fitness = newfit;
                        best = sched;
                    }
                    if (hillEvals >= maxEvals) {
                        return best;
                    }
                }
            }
            for (int j = 0; j < numTimes; j++) {
                Schedule sched = this.clone();
                if (sched.times[i].getTime() != j) {
                    sched.times[i] = new Timing(sched.times[i].getDay(), j);
                    double newfit = sched.fitness();
                    hillEvals++;
                    if (newfit > fitness) {
                        fitness = newfit;
                        best = sched;
                    }
                    if (hillEvals >= maxEvals) {
                        return best;
                    }
                }
            }
        }

//        System.out.println("Hillclimbing made " + evals + " fitness evaluations...");

        if (best == this) {
            return best;
        } else {
            return best.hillClimb(maxEvals);
        }
    }
}