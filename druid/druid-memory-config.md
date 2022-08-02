## Druid Memory Config

### Server Details

CPU = 4

total server memory = 32Gb


### Memory config used

Process       | Merge Buffers, threads, buffer size (Mb) | xms, xmx, MaxDirectMemoryDize       | TOTAL
--------------|------------------------------------------|-------------------------------------|--------------------------
historical    | 2,2,128 -> 2,2,512                       | 1048m,1048m,800m -> 2GB,5GB,2.5GB   | 7.5GB
broker 	      | -,2,128 -> 2,1,512                       | 128m,128m,800m   -> 1GB,2GB,2GB     | 4GB
coordinator   |                                          | 128m,128m,-      -> 1GB,2GB,-       | 2GB
overlord      |                                          | 256m,256m,-      -> 512GB,1GB,-     | 1GB
middlemanager |                                          | 128m,128m,-      -> 128m,128m,-     | 128m
router        |                                          | -,-,-            -> 256m,512m,-     | 512m
tasks(6->8)   | -,2,25 -> 2,2,100                        | -,900m,-         -> -,1GB,512m      | 1.5GB/task = 1.5*8 = 12GB


TOTAL = ~28GB


## Reasoning

### Historical

- druid.processing.numThreads should generally be set to (number of cores - 1): a smaller value can result in CPU underutilization, while going over the number of cores can result in unnecessary CPU contention.
	- 2 seems reasonable

- druid.processing.buffer.sizeBytes can be set to 500MB.
	- 512MB

- druid.processing.numMergeBuffers, a 1:4 ratio of merge buffers to processing threads is a reasonable choice for general use.
	- 2 * 1/4 = 0.5, lets keep it 2


Heap = (0.5GB * number of CPU cores) + (2 * total size of lookup maps) + druid.cache.sizeInBytes (=3Gb)
	 = 0.5GB * 2 (it's 4 but lets keep it at 2) + 1Gb (don't know, just guessing) + 3GB
	 = 5Gb

Direct Memory = (druid.processing.numThreads + druid.processing.numMergeBuffers + 1) * druid.processing.buffer.sizeBytes
			  = (2 + 2 + 1) * 512Mb = 2.5GB


### Broker

- druid.processing.buffer.sizeBytes can be set to 500MB.
	- 512MB
- druid.processing.numThreads: set this to 1 (the minimum allowed)
	- 1
- druid.processing.numMergeBuffers: set this to the same value as on Historicals or a bit higher
	- 2


Heap = allocated heap size (4G reasonable for 15 servers)

Direct Memory = (druid.processing.numThreads + druid.processing.numMergeBuffers + 1) * druid.processing.buffer.sizeBytes
			  = (1 + 2 + 1) * 512MB = 2GB


### Coordinator

The main performance-related setting on the Coordinator is the heap size.

You can set the Coordinator heap to the same size as your Broker heap


### Overlord


The main performance-related setting on the Overlord is the heap size.

The heap requirements of the Overlord scale primarily with the number of running Tasks.

The Overlord tends to require less resources than the Coordinator or Broker. You can generally set the Overlord heap to a value that's 25-50% of your Coordinator heap.


### MiddleManager

heap = 128MB


#### Tasks

Task Count: druid.worker.capacity=8

Task heap sizing: A 1GB heap is usually enough for Tasks.

Be sure to add (2 * total size of all loaded lookups) to your Task heap size if you are using lookups.

Task processing threads and buffers

For Tasks, 1 or 2 processing threads are often enough, as the Tasks tend to hold much less queryable data than Historical processes.

- druid.indexer.fork.property.druid.processing.numThreads: set this to 1 or 2
- druid.indexer.fork.property.druid.processing.numMergeBuffers: set this to 2
- druid.indexer.fork.property.druid.processing.buffer.sizeBytes: can be set to 100MB


Direct Memory = (druid.processing.numThreads + druid.processing.numMergeBuffers + 1) * druid.processing.buffer.sizeBytes

Heap: 1GB + (2 * total size of lookup maps)

Direct Memory: (druid.processing.numThreads + druid.processing.numMergeBuffers + 1) * druid.processing.buffer.sizeBytes


MM heap size + druid.worker.capacity * (single task memory usage)



### Router

The Router has light resource requirements, as it proxies requests to Brokers without performing much computational work itself.

You can assign it 256MB heap as a starting point, growing it if needed.

- 256m/512m
