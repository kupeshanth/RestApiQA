# Part 13 — Coding Questions | 40 Problems | Java Solutions + Explanation

> CV Context: Kupeshanth — Qoria Lanka (Selenium+TestNG, Playwright, GitHub Actions), Cerexio (Angular, Postman, JIRA). Projects: Singer Page (Serenity+Cucumber+BDD), Python+Pytest+Selenium, Playwright+JS.

> Note: These Java coding questions come up in QA/SDET interviews to test core programming skills. Know them fluently — interviewers often ask you to code live in a shared editor.

---

## Q1. Reverse a String (without StringBuilder.reverse())

**Explanation:** Iterate from the last character to the first and build a new string.

```java
public class StringProblems {

    public static String reverseString(String str) {
        if (str == null || str.isEmpty()) return str;
        char[] chars = str.toCharArray();
        int left = 0, right = chars.length - 1;
        while (left < right) {
            char temp = chars[left];
            chars[left] = chars[right];
            chars[right] = temp;
            left++;
            right--;
        }
        return new String(chars);
    }

    public static void main(String[] args) {
        System.out.println(reverseString("Kupeshanth")); // htnnahsepuK
        System.out.println(reverseString("hello"));      // olleh
        System.out.println(reverseString("a"));          // a
    }
}
```

**Key points:**
- Two-pointer technique: swap from both ends moving inward
- Time: O(n), Space: O(n) for the char array
- Interviewers check: do you handle null/empty?

---

## Q2. Check if a String is a Palindrome

**Explanation:** A palindrome reads the same forward and backward. Compare the string with its reverse, or use two pointers.

```java
public class PalindromeCheck {

    // Two-pointer approach — most efficient
    public static boolean isPalindrome(String str) {
        if (str == null) return false;
        str = str.toLowerCase().replaceAll("[^a-z0-9]", ""); // ignore case and punctuation
        int left = 0, right = str.length() - 1;
        while (left < right) {
            if (str.charAt(left) != str.charAt(right)) return false;
            left++;
            right--;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(isPalindrome("racecar"));    // true
        System.out.println(isPalindrome("A man a plan a canal Panama")); // true
        System.out.println(isPalindrome("hello"));      // false
        System.out.println(isPalindrome(""));           // true
    }
}
```

**Key points:**
- The `replaceAll("[^a-z0-9]", "")` handles spaces and punctuation in interview edge cases
- Time: O(n), Space: O(1) for two-pointer approach

---

## Q3. Fibonacci Series (first N numbers)

**Explanation:** Each number is the sum of the two preceding ones. 0, 1, 1, 2, 3, 5, 8, 13...

```java
public class Fibonacci {

    // Iterative — preferred for large N (no stack overflow)
    public static void printFibonacci(int n) {
        if (n <= 0) return;
        int a = 0, b = 1;
        System.out.print(a);
        for (int i = 1; i < n; i++) {
            System.out.print(", " + b);
            int temp = a + b;
            a = b;
            b = temp;
        }
        System.out.println();
    }

    // Recursive — elegant but O(2^n) time, use only for small N
    public static int fibRecursive(int n) {
        if (n <= 1) return n;
        return fibRecursive(n - 1) + fibRecursive(n - 2);
    }

    // Get as List
    public static List<Integer> getFibSeries(int n) {
        List<Integer> result = new ArrayList<>();
        int a = 0, b = 1;
        for (int i = 0; i < n; i++) {
            result.add(a);
            int temp = a + b;
            a = b;
            b = temp;
        }
        return result;
    }

    public static void main(String[] args) {
        printFibonacci(10); // 0, 1, 1, 2, 3, 5, 8, 13, 21, 34
        System.out.println(getFibSeries(8)); // [0, 1, 1, 2, 3, 5, 8, 13]
    }
}
```

---

## Q4. Check if a Number is Prime

**Explanation:** A prime number has exactly two factors: 1 and itself. Check divisibility up to square root.

```java
public class PrimeCheck {

    public static boolean isPrime(int n) {
        if (n < 2) return false;        // 0 and 1 are not prime
        if (n == 2) return true;        // 2 is the only even prime
        if (n % 2 == 0) return false;   // all other even numbers — not prime

        // Check odd divisors up to sqrt(n)
        // If n = 36, we only need to check up to 6 (sqrt(36))
        for (int i = 3; i <= Math.sqrt(n); i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(isPrime(2));   // true
        System.out.println(isPrime(17));  // true
        System.out.println(isPrime(18));  // false
        System.out.println(isPrime(1));   // false

        // Print all primes up to 50
        for (int i = 0; i <= 50; i++) {
            if (isPrime(i)) System.out.print(i + " ");
        }
        // 2 3 5 7 11 13 17 19 23 29 31 37 41 43 47
    }
}
```

**Key optimization:** Check only up to `Math.sqrt(n)` — if n has a divisor greater than its square root, then it has a corresponding divisor smaller than the square root.

---

## Q5. Remove Duplicates from a List

**Explanation:** LinkedHashSet preserves insertion order and removes duplicates automatically.

```java
import java.util.*;
import java.util.stream.Collectors;

public class RemoveDuplicates {

    // Using LinkedHashSet (preserves order)
    public static <T> List<T> removeDuplicates(List<T> list) {
        return new ArrayList<>(new LinkedHashSet<>(list));
    }

    // Using Streams
    public static <T> List<T> removeDuplicatesStream(List<T> list) {
        return list.stream()
            .distinct()
            .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<Integer> nums = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6, 5, 3);
        System.out.println(removeDuplicates(nums));        // [3, 1, 4, 5, 9, 2, 6]
        System.out.println(removeDuplicatesStream(nums));  // [3, 1, 4, 5, 9, 2, 6]

        List<String> names = Arrays.asList("Alice", "Bob", "Alice", "Charlie", "Bob");
        System.out.println(removeDuplicates(names)); // [Alice, Bob, Charlie]
    }
}
```

**Why LinkedHashSet?**
- HashSet removes duplicates but does NOT preserve order
- LinkedHashSet removes duplicates AND preserves insertion order

---

## Q6. Sort an Array (Bubble Sort + Collections.sort)

**Explanation:** Bubble Sort repeatedly swaps adjacent elements that are out of order.

```java
import java.util.*;

public class SortingExamples {

    // Bubble sort — O(n^2) — for demo, not production
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) break; // Already sorted — early exit optimisation
        }
    }

    // Collections.sort — O(n log n) — use this in real code
    public static List<Integer> sortList(List<Integer> list) {
        List<Integer> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        return sorted;
    }

    // Sort in reverse
    public static List<Integer> sortDescending(List<Integer> list) {
        return list.stream()
            .sorted(Comparator.reverseOrder())
            .collect(java.util.stream.Collectors.toList());
    }

    public static void main(String[] args) {
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        bubbleSort(arr);
        System.out.println(Arrays.toString(arr)); // [11, 12, 22, 25, 34, 64, 90]

        List<Integer> nums = Arrays.asList(5, 2, 8, 1, 9, 3);
        System.out.println(sortList(nums));       // [1, 2, 3, 5, 8, 9]
        System.out.println(sortDescending(nums)); // [9, 8, 5, 3, 2, 1]
    }
}
```

---

## Q7. Find the Maximum Element in an Array

**Explanation:** Iterate through the array, tracking the largest value seen so far.

```java
import java.util.*;
import java.util.stream.Collectors;

public class MaxElement {

    // Manual loop
    public static int findMax(int[] arr) {
        if (arr == null || arr.length == 0)
            throw new IllegalArgumentException("Array is empty or null");
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) max = arr[i];
        }
        return max;
    }

    // Using Streams
    public static int findMaxStream(int[] arr) {
        return Arrays.stream(arr).max().orElseThrow();
    }

    // Using Collections
    public static int findMaxList(List<Integer> list) {
        return Collections.max(list);
    }

    public static void main(String[] args) {
        int[] arr = {3, 7, 1, 9, 4, 6};
        System.out.println(findMax(arr));        // 9
        System.out.println(findMaxStream(arr));  // 9
        System.out.println(findMaxList(Arrays.asList(3, 7, 1, 9, 4, 6))); // 9
    }
}
```

---

## Q8. Count Occurrences of Each Character in a String

**Explanation:** Use a HashMap where key=character, value=count.

```java
import java.util.*;

public class CharCount {

    public static Map<Character, Integer> countChars(String str) {
        Map<Character, Integer> map = new LinkedHashMap<>();
        for (char c : str.toCharArray()) {
            map.put(c, map.getOrDefault(c, 0) + 1);
        }
        return map;
    }

    // Using Streams + groupingBy
    public static Map<Character, Long> countCharsStream(String str) {
        return str.chars()
            .mapToObj(c -> (char) c)
            .collect(java.util.stream.Collectors.groupingBy(
                c -> c, java.util.stream.Collectors.counting()
            ));
    }

    public static void main(String[] args) {
        String s = "programming";
        Map<Character, Integer> counts = countChars(s);
        counts.forEach((k, v) -> System.out.println(k + " -> " + v));
        // p->1, r->2, o->1, g->2, a->1, m->2, i->1, n->1
    }
}
```

---

## Q9. Check if Two Strings are Anagrams

**Explanation:** Two strings are anagrams if they contain the same characters in the same frequencies (order doesn't matter). "listen" and "silent" are anagrams.

```java
import java.util.Arrays;

public class AnagramCheck {

    // Sort and compare — O(n log n)
    public static boolean isAnagram(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        if (s1.length() != s2.length()) return false;
        char[] a = s1.toLowerCase().toCharArray();
        char[] b = s2.toLowerCase().toCharArray();
        Arrays.sort(a);
        Arrays.sort(b);
        return Arrays.equals(a, b);
    }

    // Frequency count — O(n), faster
    public static boolean isAnagramFast(String s1, String s2) {
        if (s1.length() != s2.length()) return false;
        int[] freq = new int[256]; // ASCII
        for (char c : s1.toCharArray()) freq[c]++;
        for (char c : s2.toCharArray()) freq[c]--;
        for (int f : freq) if (f != 0) return false;
        return true;
    }

    public static void main(String[] args) {
        System.out.println(isAnagram("listen", "silent")); // true
        System.out.println(isAnagram("hello", "world"));   // false
        System.out.println(isAnagram("Triangle", "Integral")); // true
    }
}
```

---

## Q10. Calculate Factorial (Recursive + Iterative)

**Explanation:** factorial(n) = n * (n-1) * (n-2) * ... * 1. factorial(0) = 1.

```java
import java.math.BigInteger;

public class Factorial {

    // Recursive — elegant, but risk of StackOverflow for large n
    public static long factorialRecursive(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        if (n == 0 || n == 1) return 1;
        return n * factorialRecursive(n - 1);
    }

    // Iterative — preferred
    public static long factorialIterative(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        long result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }

    // BigInteger for very large factorials (n > 20 overflows long)
    public static BigInteger factorialBig(int n) {
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) result = result.multiply(BigInteger.valueOf(i));
        return result;
    }

    public static void main(String[] args) {
        System.out.println(factorialIterative(5));  // 120
        System.out.println(factorialIterative(10)); // 3628800
        System.out.println(factorialBig(25));       // 15511210043330985984000000
    }
}
```

---

## Q11. Find Second Largest Number in Array

**Explanation:** Scan once, tracking the largest and second largest.

```java
import java.util.Arrays;

public class SecondLargest {

    public static int findSecondLargest(int[] arr) {
        if (arr == null || arr.length < 2)
            throw new IllegalArgumentException("Need at least 2 elements");

        int max = Integer.MIN_VALUE, second = Integer.MIN_VALUE;
        for (int num : arr) {
            if (num > max) {
                second = max;
                max = num;
            } else if (num > second && num != max) {
                second = num;
            }
        }
        if (second == Integer.MIN_VALUE)
            throw new RuntimeException("No second largest (all elements equal?)");
        return second;
    }

    public static void main(String[] args) {
        System.out.println(findSecondLargest(new int[]{3, 7, 1, 9, 4, 6})); // 7
        System.out.println(findSecondLargest(new int[]{10, 10, 5}));         // 5
        System.out.println(findSecondLargest(new int[]{2, 1}));              // 1
    }
}
```

---

## Q12. Reverse Words in a Sentence

**Explanation:** Split by spaces, reverse the array of words, and join them back.

```java
public class ReverseWords {

    public static String reverseWords(String sentence) {
        if (sentence == null || sentence.isBlank()) return sentence;
        String[] words = sentence.trim().split("\\s+"); // handles multiple spaces
        int left = 0, right = words.length - 1;
        while (left < right) {
            String temp = words[left];
            words[left] = words[right];
            words[right] = temp;
            left++;
            right--;
        }
        return String.join(" ", words);
    }

    public static void main(String[] args) {
        System.out.println(reverseWords("Hello World"));          // World Hello
        System.out.println(reverseWords("The quick brown fox"));  // fox brown quick The
        System.out.println(reverseWords("  spaces  between  ")); // between spaces
    }
}
```

---

## Q13. Check if a String Contains Only Digits

**Explanation:** Use Character.isDigit() or regex.

```java
public class OnlyDigits {

    // Manual check
    public static boolean isOnlyDigits(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    // Regex — more concise
    public static boolean isOnlyDigitsRegex(String str) {
        return str != null && str.matches("\\d+");
    }

    public static void main(String[] args) {
        System.out.println(isOnlyDigits("12345"));   // true
        System.out.println(isOnlyDigits("123a5"));   // false
        System.out.println(isOnlyDigits(""));        // false
        System.out.println(isOnlyDigitsRegex("007")); // true
    }
}
```

---

## Q14. Count Vowels in a String

**Explanation:** Check each character against the set {a, e, i, o, u}.

```java
import java.util.Set;

public class CountVowels {

    private static final Set<Character> VOWELS = Set.of('a','e','i','o','u','A','E','I','O','U');

    public static int countVowels(String str) {
        if (str == null) return 0;
        int count = 0;
        for (char c : str.toCharArray()) {
            if (VOWELS.contains(c)) count++;
        }
        return count;
    }

    // Stream version
    public static long countVowelsStream(String str) {
        return str.chars()
            .filter(c -> "aeiouAEIOU".indexOf(c) >= 0)
            .count();
    }

    public static void main(String[] args) {
        System.out.println(countVowels("Kupeshanth"));  // u, e, a — 3
        System.out.println(countVowels("Hello World")); // e, o, o — 3
        System.out.println(countVowels("rhythm"));      // 0
    }
}
```

---

## Q15. Find Common Elements Between Two Lists

**Explanation:** Use retainAll or Set intersection.

```java
import java.util.*;
import java.util.stream.Collectors;

public class CommonElements {

    public static <T> List<T> findCommon(List<T> list1, List<T> list2) {
        Set<T> set2 = new HashSet<>(list2);
        return list1.stream()
            .filter(set2::contains)
            .distinct() // avoid duplicates if list1 has them
            .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<Integer> a = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> b = Arrays.asList(3, 4, 5, 6, 7);
        System.out.println(findCommon(a, b)); // [3, 4, 5]

        List<String> names1 = Arrays.asList("Alice", "Bob", "Charlie");
        List<String> names2 = Arrays.asList("Bob", "Dave", "Alice");
        System.out.println(findCommon(names1, names2)); // [Alice, Bob]
    }
}
```

---

## Q16. Remove Null Values from a List

**Explanation:** Use Streams filter or removeIf.

```java
import java.util.*;
import java.util.stream.Collectors;

public class RemoveNulls {

    public static <T> List<T> removeNulls(List<T> list) {
        return list.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    // In-place modification
    public static <T> void removeNullsInPlace(List<T> list) {
        list.removeIf(Objects::isNull);
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>(Arrays.asList("a", null, "b", null, "c"));
        System.out.println(removeNulls(list)); // [a, b, c]

        removeNullsInPlace(list);
        System.out.println(list); // [a, b, c] (modified in place)
    }
}
```

---

## Q17. Group Elements of a List by Frequency

**Explanation:** Count frequencies, then group values by their count.

```java
import java.util.*;
import java.util.stream.Collectors;

public class GroupByFrequency {

    public static <T> Map<Long, List<T>> groupByFrequency(List<T> list) {
        // Step 1: count each element
        Map<T, Long> freq = list.stream()
            .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        // Step 2: group elements by their count
        Map<Long, List<T>> grouped = new LinkedHashMap<>();
        freq.forEach((elem, count) ->
            grouped.computeIfAbsent(count, k -> new ArrayList<>()).add(elem)
        );
        return grouped;
    }

    public static void main(String[] args) {
        List<String> words = Arrays.asList("apple","banana","apple","cherry","banana","apple");
        Map<Long, List<String>> result = groupByFrequency(words);
        result.forEach((count, items) ->
            System.out.println("Appears " + count + " times: " + items)
        );
        // Appears 3 times: [apple]
        // Appears 2 times: [banana]
        // Appears 1 times: [cherry]
    }
}
```

---

## Q18. Find Duplicates in an Array

**Explanation:** Use a HashSet — add each element; if add() returns false, it's a duplicate.

```java
import java.util.*;

public class FindDuplicates {

    public static List<Integer> findDuplicates(int[] arr) {
        Set<Integer> seen = new HashSet<>();
        Set<Integer> duplicates = new LinkedHashSet<>();
        for (int num : arr) {
            if (!seen.add(num)) duplicates.add(num);
        }
        return new ArrayList<>(duplicates);
    }

    public static void main(String[] args) {
        int[] arr = {1, 3, 4, 2, 2, 5, 3, 6, 1};
        System.out.println(findDuplicates(arr)); // [2, 3, 1]

        int[] noDups = {1, 2, 3, 4};
        System.out.println(findDuplicates(noDups)); // []
    }
}
```

---

## Q19. Flatten a Nested List

**Explanation:** Java doesn't have built-in nested list, so we demonstrate with List<List<T>>.

```java
import java.util.*;
import java.util.stream.Collectors;

public class FlattenList {

    public static <T> List<T> flatten(List<List<T>> nested) {
        return nested.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    // Recursive version for arbitrary depth
    @SuppressWarnings("unchecked")
    public static List<Object> flattenDeep(List<?> list) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof List) {
                result.addAll(flattenDeep((List<?>) item));
            } else {
                result.add(item);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        List<List<Integer>> nested = Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5),
            Arrays.asList(6, 7, 8)
        );
        System.out.println(flatten(nested)); // [1, 2, 3, 4, 5, 6, 7, 8]
    }
}
```

---

## Q20. Convert List<String> to Map<String, Integer> (string → its length)

**Explanation:** Use Collectors.toMap with the string as key and its length as value.

```java
import java.util.*;
import java.util.stream.Collectors;

public class ListToMap {

    public static Map<String, Integer> toStringLengthMap(List<String> list) {
        return list.stream()
            .collect(Collectors.toMap(
                s -> s,           // key: the string itself
                String::length,   // value: its length
                (existing, replacement) -> existing // merge: keep first on duplicate key
            ));
    }

    public static void main(String[] args) {
        List<String> words = Arrays.asList("apple", "banana", "kiwi", "mango");
        Map<String, Integer> result = toStringLengthMap(words);
        result.forEach((k, v) -> System.out.println(k + " -> " + v));
        // apple -> 5
        // banana -> 6
        // kiwi -> 4
        // mango -> 5
    }
}
```

---

## Q21. Find First Non-Repeating Character in a String

**Explanation:** Count character frequencies, then scan again to find the first with count = 1.

```java
import java.util.LinkedHashMap;
import java.util.Map;

public class FirstNonRepeating {

    public static Character firstNonRepeating(String str) {
        if (str == null || str.isEmpty()) return null;

        // LinkedHashMap preserves insertion order
        Map<Character, Integer> freq = new LinkedHashMap<>();
        for (char c : str.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }

        for (Map.Entry<Character, Integer> entry : freq.entrySet()) {
            if (entry.getValue() == 1) return entry.getKey();
        }
        return null; // all characters repeat
    }

    public static void main(String[] args) {
        System.out.println(firstNonRepeating("aabcde"));   // b (a repeats, b is first unique)
        System.out.println(firstNonRepeating("aabb"));      // null
        System.out.println(firstNonRepeating("swiss"));     // w
        System.out.println(firstNonRepeating("programming")); // p (r,g,m repeat)
    }
}
```

---

## Q22. Check Balanced Parentheses

**Explanation:** Use a stack. Push opening brackets; when you see a closing bracket, check the stack top matches.

```java
import java.util.Deque;
import java.util.ArrayDeque;

public class BalancedParentheses {

    public static boolean isBalanced(String str) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : str.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (c == ')' || c == ']' || c == '}') {
                if (stack.isEmpty()) return false;
                char top = stack.pop();
                if ((c == ')' && top != '(') ||
                    (c == ']' && top != '[') ||
                    (c == '}' && top != '{')) {
                    return false;
                }
            }
        }
        return stack.isEmpty(); // stack must be empty — all opened brackets were closed
    }

    public static void main(String[] args) {
        System.out.println(isBalanced("()[]{}"));     // true
        System.out.println(isBalanced("([{}])"));     // true
        System.out.println(isBalanced("([)]"));       // false
        System.out.println(isBalanced("{[}"));        // false
        System.out.println(isBalanced("("));          // false
    }
}
```

---

## Q23. Merge Two Sorted Arrays

**Explanation:** Use two pointers, comparing elements from each array and adding the smaller one.

```java
import java.util.Arrays;

public class MergeSortedArrays {

    public static int[] mergeSorted(int[] a, int[] b) {
        int[] result = new int[a.length + b.length];
        int i = 0, j = 0, k = 0;

        while (i < a.length && j < b.length) {
            if (a[i] <= b[j]) result[k++] = a[i++];
            else               result[k++] = b[j++];
        }
        // Copy remaining elements
        while (i < a.length) result[k++] = a[i++];
        while (j < b.length) result[k++] = b[j++];

        return result;
    }

    public static void main(String[] args) {
        int[] a = {1, 3, 5, 7};
        int[] b = {2, 4, 6, 8, 10};
        System.out.println(Arrays.toString(mergeSorted(a, b)));
        // [1, 2, 3, 4, 5, 6, 7, 8, 10]
    }
}
```

---

## Q24. Find Longest Common Prefix in an Array of Strings

**Explanation:** Use the first string as baseline. Keep shrinking it while it's not a prefix of every other string.

```java
public class LongestCommonPrefix {

    public static String longestCommonPrefix(String[] strs) {
        if (strs == null || strs.length == 0) return "";
        String prefix = strs[0];
        for (int i = 1; i < strs.length; i++) {
            while (!strs[i].startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) return "";
            }
        }
        return prefix;
    }

    public static void main(String[] args) {
        System.out.println(longestCommonPrefix(new String[]{"flower","flow","flight"})); // fl
        System.out.println(longestCommonPrefix(new String[]{"dog","car","race"}));       // ""
        System.out.println(longestCommonPrefix(new String[]{"interview","internal","inter"})); // inter
    }
}
```

---

## Q25. Print a Pyramid Pattern of Stars

**Explanation:** Nested loops — outer loop controls rows, inner loops control spaces and stars.

```java
public class PyramidPattern {

    public static void printPyramid(int rows) {
        for (int i = 1; i <= rows; i++) {
            // Print leading spaces
            for (int s = rows - i; s > 0; s--) System.out.print(" ");
            // Print stars (2*i - 1 stars in row i)
            for (int j = 1; j <= 2 * i - 1; j++) System.out.print("*");
            System.out.println();
        }
    }

    // Inverted pyramid
    public static void printInvertedPyramid(int rows) {
        for (int i = rows; i >= 1; i--) {
            for (int s = rows - i; s > 0; s--) System.out.print(" ");
            for (int j = 1; j <= 2 * i - 1; j++) System.out.print("*");
            System.out.println();
        }
    }

    public static void main(String[] args) {
        printPyramid(5);
        /*
            *
           ***
          *****
         *******
        *********
        */
    }
}
```

---

## Q26. Sum of Digits of a Number

**Explanation:** Use modulo 10 to get the last digit, divide by 10 to remove it, repeat.

```java
public class SumOfDigits {

    // Iterative
    public static int sumOfDigits(int n) {
        n = Math.abs(n); // handle negatives
        int sum = 0;
        while (n > 0) {
            sum += n % 10; // last digit
            n /= 10;       // remove last digit
        }
        return sum;
    }

    // Recursive
    public static int sumOfDigitsRecursive(int n) {
        n = Math.abs(n);
        if (n < 10) return n;
        return n % 10 + sumOfDigitsRecursive(n / 10);
    }

    // String approach (easier to read)
    public static int sumOfDigitsString(int n) {
        return String.valueOf(Math.abs(n))
            .chars()
            .map(Character::getNumericValue)
            .sum();
    }

    public static void main(String[] args) {
        System.out.println(sumOfDigits(12345));    // 15
        System.out.println(sumOfDigits(-456));     // 15
        System.out.println(sumOfDigitsString(9876)); // 30
    }
}
```

---

## Q27. Find All Substrings of a String

**Explanation:** Use nested loops: outer loop sets the start, inner loop sets the end.

```java
import java.util.ArrayList;
import java.util.List;

public class AllSubstrings {

    public static List<String> findAllSubstrings(String str) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < str.length(); i++) {
            for (int j = i + 1; j <= str.length(); j++) {
                result.add(str.substring(i, j));
            }
        }
        return result;
    }

    public static void main(String[] args) {
        List<String> subs = findAllSubstrings("abc");
        System.out.println(subs); // [a, ab, abc, b, bc, c]
        System.out.println("Total substrings of 'abc': " + subs.size()); // 6
    }
}
```

---

## Q28. Check if LinkedList Has a Cycle

**Explanation:** Floyd's Cycle Detection — use two pointers (slow moves 1 step, fast moves 2 steps). If they meet, there's a cycle.

```java
public class CycleDetection {

    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    public static boolean hasCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;           // 1 step
            fast = fast.next.next;      // 2 steps
            if (slow == fast) return true; // they met — cycle detected
        }
        return false; // fast reached end — no cycle
    }

    public static void main(String[] args) {
        // No cycle: 1 -> 2 -> 3 -> null
        ListNode head = new ListNode(1);
        head.next = new ListNode(2);
        head.next.next = new ListNode(3);
        System.out.println(hasCycle(head)); // false

        // Cycle: 1 -> 2 -> 3 -> back to 2
        head.next.next.next = head.next;
        System.out.println(hasCycle(head)); // true
    }
}
```

---

## Q29. Binary Search Implementation

**Explanation:** Works only on sorted arrays. Compare the target with the middle element and narrow the search range.

```java
import java.util.Arrays;

public class BinarySearch {

    // Iterative — preferred
    public static int binarySearch(int[] arr, int target) {
        int left = 0, right = arr.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2; // avoid integer overflow
            if (arr[mid] == target) return mid;
            if (arr[mid] < target) left = mid + 1;
            else right = mid - 1;
        }
        return -1; // not found
    }

    // Recursive
    public static int binarySearchRecursive(int[] arr, int target, int left, int right) {
        if (left > right) return -1;
        int mid = left + (right - left) / 2;
        if (arr[mid] == target) return mid;
        if (arr[mid] < target) return binarySearchRecursive(arr, target, mid + 1, right);
        return binarySearchRecursive(arr, target, left, mid - 1);
    }

    public static void main(String[] args) {
        int[] arr = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};
        System.out.println(binarySearch(arr, 23));  // 5 (index)
        System.out.println(binarySearch(arr, 99));  // -1 (not found)
        System.out.println(Arrays.binarySearch(arr, 16)); // 4 (built-in)
    }
}
```

---

## Q30. Remove a Character from String Without replace()

**Explanation:** Build a new string, copying all characters except the one to remove.

```java
public class RemoveChar {

    public static String removeChar(String str, char target) {
        if (str == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c != target) sb.append(c);
        }
        return sb.toString();
    }

    // Remove all occurrences at given index (remove by position)
    public static String removeAtIndex(String str, int index) {
        if (index < 0 || index >= str.length()) throw new IndexOutOfBoundsException();
        return str.substring(0, index) + str.substring(index + 1);
    }

    public static void main(String[] args) {
        System.out.println(removeChar("hello world", 'l')); // "heo word"
        System.out.println(removeChar("banana", 'a'));       // "bnn"
        System.out.println(removeAtIndex("hello", 1));      // "hllo" (removes 'e')
    }
}
```

---

## Q31. Count Words in a Sentence

**Explanation:** Split by whitespace and count the resulting parts.

```java
public class WordCount {

    public static int countWords(String sentence) {
        if (sentence == null || sentence.isBlank()) return 0;
        String[] words = sentence.trim().split("\\s+"); // \\s+ handles multiple spaces
        return words.length;
    }

    // Also count specific word
    public static int countSpecificWord(String sentence, String word) {
        String[] words = sentence.trim().split("\\s+");
        int count = 0;
        for (String w : words) {
            if (w.equalsIgnoreCase(word)) count++;
        }
        return count;
    }

    public static void main(String[] args) {
        System.out.println(countWords("The quick brown fox"));   // 4
        System.out.println(countWords("  hello   world  "));    // 2
        System.out.println(countSpecificWord("the cat and the dog", "the")); // 2
    }
}
```

---

## Q32. Find the Most Frequent Element in a List

**Explanation:** Count frequencies with HashMap, then find the entry with the max value.

```java
import java.util.*;

public class MostFrequent {

    public static <T> T findMostFrequent(List<T> list) {
        if (list == null || list.isEmpty()) throw new IllegalArgumentException("List is empty");
        Map<T, Integer> freq = new HashMap<>();
        for (T item : list) freq.put(item, freq.getOrDefault(item, 0) + 1);
        return Collections.max(freq.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public static void main(String[] args) {
        List<Integer> nums = Arrays.asList(1, 3, 2, 3, 4, 3, 2);
        System.out.println(findMostFrequent(nums)); // 3

        List<String> words = Arrays.asList("apple", "banana", "apple", "cherry", "apple");
        System.out.println(findMostFrequent(words)); // apple
    }
}
```

---

## Q33. Check if String Has All Unique Characters

**Explanation:** Use a HashSet — if adding a character returns false, it's a duplicate.

```java
import java.util.HashSet;
import java.util.Set;

public class UniqueChars {

    public static boolean hasAllUnique(String str) {
        if (str == null) return true;
        Set<Character> seen = new HashSet<>();
        for (char c : str.toCharArray()) {
            if (!seen.add(c)) return false; // add() returns false if already present
        }
        return true;
    }

    // Without extra data structure (O(n^2))
    public static boolean hasAllUniqueNoDS(String str) {
        for (int i = 0; i < str.length(); i++) {
            for (int j = i + 1; j < str.length(); j++) {
                if (str.charAt(i) == str.charAt(j)) return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(hasAllUnique("abcde"));   // true
        System.out.println(hasAllUnique("hello"));   // false (l repeats)
        System.out.println(hasAllUnique("abcdefghijklmnopqrstuvwxyz")); // true
    }
}
```

---

## Q34. Rotate an Array by N Positions

**Explanation:** Reverse the whole array, then reverse first k elements, then reverse the rest.

```java
import java.util.Arrays;

public class RotateArray {

    public static void rotate(int[] arr, int k) {
        int n = arr.length;
        k = k % n; // handle k > n
        reverse(arr, 0, n - 1);
        reverse(arr, 0, k - 1);
        reverse(arr, k, n - 1);
    }

    private static void reverse(int[] arr, int start, int end) {
        while (start < end) {
            int temp = arr[start]; arr[start] = arr[end]; arr[end] = temp;
            start++; end--;
        }
    }

    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5, 6, 7};
        rotate(arr, 3);
        System.out.println(Arrays.toString(arr)); // [5, 6, 7, 1, 2, 3, 4]
    }
}
```

---

## Q35. Two Sum Problem (Find Indices That Add to Target)

**Explanation:** Use a HashMap to store each number and its index. For each element, check if the complement (target - num) is already in the map.

```java
import java.util.*;

public class TwoSum {

    public static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>(); // value -> index
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (map.containsKey(complement)) {
                return new int[]{map.get(complement), i};
            }
            map.put(nums[i], i);
        }
        throw new IllegalArgumentException("No two sum solution found");
    }

    public static void main(String[] args) {
        int[] nums = {2, 7, 11, 15};
        System.out.println(Arrays.toString(twoSum(nums, 9)));  // [0, 1] (2+7=9)
        System.out.println(Arrays.toString(twoSum(new int[]{3, 2, 4}, 6))); // [1, 2]
    }
}
```

---

## Q36. FizzBuzz (Classic)

**Explanation:** Print 1 to 100. For multiples of 3 print "Fizz", multiples of 5 print "Buzz", both print "FizzBuzz".

```java
public class FizzBuzz {

    public static void fizzBuzz(int n) {
        for (int i = 1; i <= n; i++) {
            if (i % 15 == 0)      System.out.println("FizzBuzz"); // check 15 FIRST
            else if (i % 3 == 0)  System.out.println("Fizz");
            else if (i % 5 == 0)  System.out.println("Buzz");
            else                  System.out.println(i);
        }
    }

    // With StringBuilder — extensible (can add "Jazz" for 7, etc.)
    public static String fizzBuzzString(int n) {
        StringBuilder sb = new StringBuilder();
        sb.append(n % 3 == 0 ? "Fizz" : "");
        sb.append(n % 5 == 0 ? "Buzz" : "");
        return sb.length() == 0 ? String.valueOf(n) : sb.toString();
    }

    public static void main(String[] args) {
        fizzBuzz(20);
        // 1, 2, Fizz, 4, Buzz, Fizz, 7, 8, Fizz, Buzz, 11, Fizz, 13, 14, FizzBuzz, ...
    }
}
```

---

## Q37. Power of 2 Check

**Explanation:** Powers of 2 have exactly one bit set in binary. Bitwise AND with (n-1) = 0 for powers of 2.

```java
public class PowerOfTwo {

    // Bitwise trick — O(1)
    public static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
        // n=8: 1000 & 0111 = 0 → true
        // n=6: 0110 & 0101 = 0100 ≠ 0 → false
    }

    // Loop approach
    public static boolean isPowerOfTwoLoop(int n) {
        if (n <= 0) return false;
        while (n > 1) {
            if (n % 2 != 0) return false;
            n /= 2;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(isPowerOfTwo(1));   // true (2^0)
        System.out.println(isPowerOfTwo(16));  // true (2^4)
        System.out.println(isPowerOfTwo(18));  // false
        System.out.println(isPowerOfTwo(0));   // false
    }
}
```

---

## Q38. Sum All Even Numbers in a List Using Streams

**Explanation:** Filter for even, then sum using Stream's sum() or reduce().

```java
import java.util.Arrays;
import java.util.List;

public class SumEvenStreams {

    public static int sumEven(List<Integer> list) {
        return list.stream()
            .filter(n -> n % 2 == 0)
            .mapToInt(Integer::intValue)
            .sum();
    }

    // Using reduce
    public static int sumEvenReduce(List<Integer> list) {
        return list.stream()
            .filter(n -> n % 2 == 0)
            .reduce(0, Integer::sum);
    }

    public static void main(String[] args) {
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println(sumEven(nums));        // 2+4+6+8+10 = 30
        System.out.println(sumEvenReduce(nums));  // 30
    }
}
```

---

## Q39. Filter Strings Longer Than 5 Chars Using Streams

**Explanation:** Use stream filter() with a length condition.

```java
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FilterStrings {

    public static List<String> longerThan5(List<String> words) {
        return words.stream()
            .filter(w -> w.length() > 5)
            .collect(Collectors.toList());
    }

    // Bonus: sort by length, uppercase, and collect
    public static List<String> processWords(List<String> words) {
        return words.stream()
            .filter(w -> w.length() > 5)
            .map(String::toUpperCase)
            .sorted()
            .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<String> words = Arrays.asList(
            "cat", "elephant", "dog", "kangaroo", "bee", "dolphin", "ant"
        );
        System.out.println(longerThan5(words));   // [elephant, kangaroo, dolphin]
        System.out.println(processWords(words));  // [DOLPHIN, ELEPHANT, KANGAROO]
    }
}
```

---

## Q40. Group Strings by Length Using Streams + Collectors.groupingBy

**Explanation:** groupingBy creates a Map where the key is the classifier result (string length) and the value is a List of matching elements.

```java
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupByLength {

    public static Map<Integer, List<String>> groupByLength(List<String> words) {
        return words.stream()
            .collect(Collectors.groupingBy(String::length));
    }

    // Bonus: count elements in each group
    public static Map<Integer, Long> countByLength(List<String> words) {
        return words.stream()
            .collect(Collectors.groupingBy(String::length, Collectors.counting()));
    }

    public static void main(String[] args) {
        List<String> words = Arrays.asList(
            "cat", "dog", "bear", "lion", "elephant", "ant", "bee", "tiger"
        );

        Map<Integer, List<String>> grouped = groupByLength(words);
        grouped.forEach((len, list) ->
            System.out.println("Length " + len + ": " + list)
        );
        // Length 3: [cat, dog, ant, bee]
        // Length 4: [bear, lion]
        // Length 5: [tiger]
        // Length 8: [elephant]

        System.out.println("\nCounts:");
        countByLength(words).forEach((len, count) ->
            System.out.println("Length " + len + ": " + count + " words")
        );
    }
}
```

---

## Quick Reference — Common Stream Operations

```java
List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// Filter + Collect
List<Integer> evens = nums.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());

// Map (transform)
List<String> strings = nums.stream().map(String::valueOf).collect(Collectors.toList());

// Sum / Average / Min / Max
int sum = nums.stream().mapToInt(Integer::intValue).sum();
double avg = nums.stream().mapToInt(Integer::intValue).average().orElse(0);
int max = nums.stream().mapToInt(Integer::intValue).max().orElse(0);

// Count
long count = nums.stream().filter(n -> n > 5).count();

// AnyMatch / AllMatch / NoneMatch
boolean anyEven = nums.stream().anyMatch(n -> n % 2 == 0);  // true
boolean allPos  = nums.stream().allMatch(n -> n > 0);       // true
boolean noneNeg = nums.stream().noneMatch(n -> n < 0);      // true

// Sorted
List<Integer> sorted = nums.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());

// Distinct
List<Integer> distinct = Arrays.asList(1,2,2,3,3,3).stream().distinct().collect(Collectors.toList());

// FlatMap (flatten nested lists)
List<List<Integer>> nested = Arrays.asList(Arrays.asList(1,2), Arrays.asList(3,4));
List<Integer> flat = nested.stream().flatMap(Collection::stream).collect(Collectors.toList());

// GroupingBy
Map<Integer, List<Integer>> byMod3 = nums.stream()
    .collect(Collectors.groupingBy(n -> n % 3));

// Joining
String joined = Stream.of("a","b","c").collect(Collectors.joining(", ", "[", "]")); // [a, b, c]
```
