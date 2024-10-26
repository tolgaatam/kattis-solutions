package main

func partition[E any](arr []E, low int, high int, lessFunc func(a E, b E) bool) int {
	pivot := arr[high]
	i := low
	for j := low; j < high; j++ {
		if lessFunc(arr[j], pivot) {
			arr[i], arr[j] = arr[j], arr[i]
			i++
		}
	}
	arr[i], arr[high] = arr[high], arr[i]
	return i
}

func quickSortInternal[E any](arr []E, low int, high int, lessFunc func(a E, b E) bool) {
	if low < high {
		var p int
		p = partition(arr, low, high, lessFunc)
		quickSortInternal(arr, low, p-1, lessFunc)
		quickSortInternal(arr, p+1, high, lessFunc)
	}
}

func QuickSort[E any](arr []E, lessFunc func(a E, b E) bool) {
	quickSortInternal(arr, 0, len(arr)-1, lessFunc)
}
