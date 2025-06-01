#!/bin/bash

# for i in {1..10}
# do
#     branch="server-v$i"
    
#     # Delete the branch locally
#     git branch -D $branch
#     echo "Deleted local branch $branch"
    
#     # Delete the branch from the remote repository
#     git push origin --delete $branch
#     echo "Deleted remote branch $branch"
# done


for i in {1..10}
do
    branch="server-v$i"
    
    # Create the branch locally
    git checkout -b $branch
    
    # Create the branch in the remote repository
    git push origin -u $branch
done



# git rev-list --left-right --count main...server-v4

# curl http://localhost:11434/api/chat -d '{
#   "model": "llama223.1",
#   "messages": [
#     { "role": "user", "content": "why is the sky blue?" }
#   ],
#   "keep_alive": 0
# }'