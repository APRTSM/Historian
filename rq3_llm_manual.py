import pandas as pd
from tqdm import tqdm
import google.generativeai as genai
import time

# Configure Gemini
genai.configure(api_key="AIzaSyAb0MrEphjU_60Z1cHsIh_pDk764JYGnzs")

# Load the CSV
df = pd.read_csv("./rq3/stratified_samples_interleaved-v1.csv")

# Initialize model
model = genai.GenerativeModel('gemini-2.5-flash-lite')

PROMPT_TEMPLATE = """
You are checking if a zero-shot classifier correctly extracted the label from an LLM response.

## IMPORTANT: Your job is NOT to evaluate if the LLM is correct about the code.
## Your job is ONLY to check: Does the zero-shot label match what the LLM is SAYING?

## Task Type
The LLM was asked one of these questions:
1. "Are these patches semantically similar/identical?" → Expected answer: "yes" or "no"
2. "What type of clone are these patches?" → Expected answer: "type-1", "type-2", "type-3", "type-4", or "not-clone"

## Valid labels for this row: {valid_labels}
## Zero-shot predicted label: {zeroshot_label}

## LLM Response:
{response}

## Your Task
Check if the zero-shot label matches the LLM's conclusion.

Examples:
- LLM says "they are not semantically equivalent" → LLM means "no" → if zeroshot_label is "no" → True
- LLM says "yes, they are similar" → LLM means "yes" → if zeroshot_label is "yes" → True
- LLM says "This is a Type-3 clone" → LLM means "type-3" → if zeroshot_label is "type-3" → True
- LLM says "they are not clones" → LLM means "not-clone" or "no" → if zeroshot_label matches → True
- LLM says "not semantically equivalent" → LLM means "no" → if zeroshot_label is "yes" → False
- LLM doesn't give a clear yes/no or clone type → Unclear

## Answer with ONLY one word:
- True (zero-shot label MATCHES what the LLM is saying)
- False (zero-shot label does NOT MATCH what the LLM is saying)
- Unclear (not prefered answer, LLM response is ambiguous, contradictory, or gives no clear answer)
"""


def validate_label(row, retry_delay=5):
    """Send one row to Gemini for validation - keeps trying forever"""
    prompt = PROMPT_TEMPLATE.format(
        valid_labels=row['valid_labels'],
        zeroshot_label=row['zeroshot_label'],
        response=row['response']
    )
    
    attempt = 0
    while True:
        try:
            response = model.generate_content(prompt)
            answer = response.text.strip().lower()
            
            # Normalize answer
            if 'true' in answer:
                return 'True'
            elif 'false' in answer:
                return 'False'
            elif 'unclear' in answer:
                return 'Unclear'
            else:
                # Unexpected answer, retry
                attempt += 1
                print(f"Unexpected answer: '{answer}', retry #{attempt}...")
                time.sleep(retry_delay)
                
        except Exception as e:
            attempt += 1
            print(f"Attempt #{attempt} failed: {e}")
            print(f"Waiting {retry_delay} seconds...")
            time.sleep(retry_delay)


def main():
    print(f"Total rows to validate: {len(df)}")
    
    manual_labels = []
    
    for idx, row in tqdm(df.iterrows(), total=len(df), desc="Validating"):
        label = validate_label(row)
        manual_labels.append(label)
        
        # Save progress every 100 rows
        if (idx + 1) % 100 == 0:
            df_temp = df.iloc[:idx+1].copy()
            df_temp['manual_label'] = manual_labels
            df_temp.to_csv("./rq3/stratified_samples_labeled_progress.csv", index=False)
            print(f"\nProgress saved at row {idx + 1}")
    
    # Final save
    df['manual_label'] = manual_labels
    df.to_csv("./rq3/stratified_samples_labeled.csv", index=False)
    
    # Summary
    print("\n" + "=" * 50)
    print("SUMMARY")
    print("=" * 50)
    print(f"\nLabel distribution:")
    print(df['manual_label'].value_counts())
    
    accuracy = (df['manual_label'] == 'True').sum() / len(df) * 100
    print(f"\nZero-shot accuracy: {accuracy:.2f}%")


if __name__ == "__main__":
    main()