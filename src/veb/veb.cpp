#include <cstdint>
#include <cstddef>
#include <stdio.h>
#include <cstring>
#include <unordered_map>

// #define MAP

template<uint32_t W>
class veb_my {
	static const uint64_t NO = -1;
	static const uint32_t lo_len = (W + 1) / 2, hi_len = W / 2;
	#ifdef MAP
	std::unordered_map<uint64_t, veb_my<lo_len>*> clusters;
	#else
	veb_my<lo_len> **clusters = nullptr;
	#endif
	veb_my<lo_len> *aux = nullptr;
	uint64_t min, max;

	uint64_t get_lo(uint64_t x) {
       	// if w % 2 == 1 then lower part is 1 bit bigger
       	return x & ((1L << lo_len) - 1);
    }

    uint64_t get_hi(uint64_t x) {
       	// if w % 2 == 1 then upper part is 1 bit smaller
       	return x >> lo_len;
    }

    uint64_t join(uint64_t hi, uint64_t lo) {
       	return (hi << lo_len) | lo;
   	}

    veb_my<lo_len>* get_cluster(uint64_t index) {
    	#ifdef MAP
       	if (clusters.find(index) == clusters.end())
       		clusters.insert({index, new veb_my<lo_len>()});
       	#else
    	if (clusters[index] == nullptr)
			clusters[index] = new veb_my<lo_len>();
		#endif
       	return clusters[index];
   	}

    void init() {
    	#ifdef MAP
       	if (aux == nullptr)
       		aux = new veb_my<lo_len>();
       	#else
       	if (clusters == nullptr) {
            clusters = new veb_my<lo_len>*[1 << hi_len];
            memset(clusters, 0, (1 << hi_len) * sizeof(size_t));
            aux = new veb_my<lo_len>();
       	}
       	#endif
   	}

public:

	veb_my() {
		min = max = NO;
	}

	~veb_my() {
		#ifdef MAP
		for (std::pair<uint64_t, veb_my<lo_len>*> x : clusters)
			delete x.second;
		if (aux != nullptr)
			delete aux;
		#else 
		if (clusters != nullptr) {
			for (size_t i = 0; i < 1 << hi_len; i++) {
				if (clusters[i] != nullptr) {
					delete clusters[i];
				} 
			}
			delete[] clusters;
			delete aux;
		}
		#endif
	}

    void add(uint64_t x) {
       	if (min == NO) {
           	min = max = x;
           	return;
       	}
       	if (x == min) {
           	return;
      	}
       	if (x < min) {
           	uint64_t t = x;
           	x = min;
           	min = t;
       	}
       	if (x > max) {
           	max = x;
       	}
       	init();
       	uint64_t hi = get_hi(x), lo = get_lo(x);
       	veb_my<lo_len>* cluster = get_cluster(hi);
       	if (cluster->get_min() == NO) {
           	aux->add(hi);
       	}
       	cluster->add(lo);
   	}

    void remove(uint64_t x) {
       	init();
       	if (x == min) {
           	if (aux->get_min() == NO) {
               	min = max = NO;
           	} else {
               	uint64_t min_hi = aux->get_min();
               	veb_my<lo_len>* cluster = get_cluster(min_hi);
               	min = join(min_hi, cluster->get_min());
               	cluster->remove(cluster->get_min());
               	if (cluster->get_min() == NO)
                   	aux->remove(min_hi);
           	}
       	} else {
           	uint64_t hi = get_hi(x), lo = get_lo(x);
           	veb_my<lo_len>* cluster = get_cluster(hi);
           	cluster->remove(lo);
           	if (cluster->get_min() == NO)
               	aux->remove(hi);
           	if (aux->get_min() == NO) {
               	max = min;
           	} else {
               	max = join(aux->get_max(), get_cluster(aux->get_max())->get_max());
           	}
       	}
   	}

    uint64_t next(uint64_t x) {
       	if (min == NO)
           	return NO;
       	if (x < min)
           	return min;
       	init();
       	uint64_t hi = get_hi(x), lo = get_lo(x);
       	veb_my<lo_len>* cluster = get_cluster(hi);
       	if (cluster->get_min() != NO && lo < cluster->get_max()) {
           	return join(hi, cluster->next(lo));
       	}
       	uint64_t next_hi = aux->next(hi);
       	if (next_hi == NO) {
           	return NO;
       	}
       	cluster = get_cluster(next_hi);
       	return join(next_hi, cluster->get_min());
   	}

   	uint64_t prev(uint64_t x) {
       	if (min == NO || x <= min)
           	return NO;
       	if (x > max)
           	return max;
       	init();
       	uint64_t hi = get_hi(x), lo = get_lo(x);
       	veb_my<lo_len>* cluster = get_cluster(hi);
       	if (cluster->get_min() != NO && lo > cluster->get_min()) {
           	return join(hi, cluster->prev(lo));
       	}
       	uint64_t prev_hi = aux->prev(hi);
       	if (prev_hi == NO) {
           	return min;
       	}
       	return join(prev_hi, get_cluster(prev_hi)->get_max());
   	}

    uint64_t get_min() {
       	return min;
   	}

    uint64_t get_max() {
       	return max;
   	}
};

template<>
class veb_my<1> {
	static const uint64_t NO = -1; 

	bool data[2];

public:

    veb_my() { 
       	data[0] = data[1] = 0;
	}

	void add(uint64_t x) {
	   	data[x] = true;
	}

	void remove(uint64_t x) {
	   	data[x] = false;
	}

	uint64_t next(uint64_t x) {
	   	if (x == 1) {
	       	return NO;
	   	} else {
	       	return data[1] ? 1 : NO;
	   	}
	}

	uint64_t prev(uint64_t x) {
	   	if (x == 0) {
	       	return NO;
	   	} else {
	       	return data[0] ? 0 : NO;
	   	}
	}

	uint64_t get_min() {
	   	if (data[0]) {
	       	return 0;
	   	} else if (data[1]) {
	       	return 1;
	   	}
	   	return NO;
	}

	uint64_t get_max() {
	   	if (data[1]) {
	       	return 1;
	   	} else if (data[0]) {
	       	return 0;
	   	}
	   	return NO;
	}
};

// int main() {
// 	veb_my<28> v;
// 	size_t n = 50000;
// 	for (size_t i = 0; i < n; i++)
// 		v.add(i);
// 	for (size_t i = 0; i < n - 1; i++)
// 		if (v.next(i) != i + 1)
// 			printf("err: %d\n", v.next(i)); 
// }