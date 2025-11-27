public static void evenOdd(IntList lst) {
    if (lst == null || lst.rest == null || lst.rest.rest == null) {
        return;
    }
    IntList second = lst.rest;
    int index = 0;
    while (!(index % 2 == 0 && (lst.rest == null || lst.rest.rest == null))) {
        IntList temp = lst.rest;
        lst.rest = lst.rest.rest;
        lst = temp;
        index++;
    }
    lst.rest = second;
}
//先不看index，核心思路就是每一个lst.next切换到next.next，然后用temp缓存最开始的next，不然丢了
//然后这样就确保while !(lst.rest == null || lst.rest.rest == null)，然后再说index，如果满足
//前两个条件的话，这时候要分奇偶，如果index是偶数,如0123，那么下一次2后面接null，但是lst变成了3
//而如果是这样的话，奇数后面不能接奇数，所以index只能以奇数结尾
//这个是一个指针来回每个都动，另一个是两个指针来回动，
/**
 * 1. 视角的区别：并行 (Parallel) vs 串行 (Serial)
    双指针法 (Method 1) 是 “并行视角”：
        把链表看作 两列队伍（偶数队、奇数队）。
        每次循环处理 一对（一个偶、一个奇）。
        效率感觉上快一倍（虽然时间复杂度都是 O(N)，但循环次数少一半）。
    单指针法 (Method 2) 是 “串行视角”：
        把链表看作 一条流水线。
        不管你是谁，来了我就处理。
        每次循环只处理 一个 节点。
2. 身份识别的区别：变量名 vs 计数器
    双指针法 不需要智商：
        even 变量指的一定是偶数，odd 变量指的一定是奇数。
        身份是“自带”的，不需要判断。
    单指针法 需要带脑子：
        lst 只是个指针，它自己不知道自己踩在偶数上还是奇数上。
        所以必须额外背一个 index 计数器，每走一步都要算一下 index % 2 才能知道自己现在的身份。
3. 停车难度的区别：自然停车 vs 紧急刹车
    双指针法 是 “自然停车”：
        只要奇数队没路了，或者奇数队后面没人了，循环自然就停了。
        因为 even 永远在 odd 前面（或者平行），只要检查 odd 就够了，非常安全。
    单指针法 是 “紧急刹车”：
        因为它是一步步走的，它很容易走过头（走到奇数尾巴上去）。
        所以它必须每一步都战战兢兢地检查：“我是不是偶数？后面是不是没路了？如果是，赶紧踩刹车！”
        这就是为什么它的判定条件那么苛刻且丑陋。
 */